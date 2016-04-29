set showcmd
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
set cursorline
filetype indent on
set lazyredraw
set showmatch
colorscheme nightflight2
