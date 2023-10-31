TARGET_FILE=$1

echo "target file: ${TARGET_FILE}"

mkdir ./tmp

cat > ./tmp/libsysy.c << EOF
#include<stdio.h>
#include<stdarg.h>
#include<sys/time.h>

/* Input & output functions */
int getint(),getch(),getarray(int a[]);
void putint(int a),putch(int a),putarray(int n,int a[]);

/* Input & output functions */
int getint(){int t; scanf("%d",&t); return t; }
int getch(){char c; scanf("%c",&c); return (int)c; }
int getarray(int a[]){
    int n;
    scanf("%d",&n);
    for(int i=0;i<n;i++)scanf("%d",&a[i]);
    return n;
}
void putint(int a){ printf("%d",a);}
void putch(int a){ printf("%c",a); }
void putarray(int n,int a[]){
    printf("%d:",n);
    for(int i=0;i<n;i++)printf(" %d",a[i]);
    printf("\n");
}
EOF

clang -emit-llvm -S ./tmp/libsysy.c -o ./tmp/lib.ll

llvm-link $TARGET_FILE ./tmp/lib.ll -S -o ./tmp/out.ll

lli ./tmp/out.ll

rm ./tmp -r
