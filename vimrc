set showcmd
set hlsearch
set smarttab
set tabstop=2
set softtabstop=2
set shiftwidth=2
set expandtab
set mouse=a
set nowrap
set showmode
set backspace=indent,eol,start
set title
set autoindent
set smartindent
set autowrite
set number

if has("autocmd")
  filetype plugin indent on
  autocmd BufReadPost *
    \ if line("'\"") > 1 && line("'\"") <= line("$") |
    \   exe "normal! g`\"" |
    \ endif
endif


syntax on
filetype indent on
set lazyredraw
set showmatch
colorscheme elflord
