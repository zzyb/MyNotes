"jk 运行ESC
inoremap jk <esc>

"显示行号
:set nu

"自动语法高亮
syntax on

"设定配色方案
"

"突出显示当前行
:set cursorline

"共享剪切板
set clipboard+=unnamed

vmap <C-c> "+yi
vmap <C-x> "+c
vmap <C-v> c<ESC>"+p
imap <C-v> <C-r><C-o>+

