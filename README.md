# BUAA-Compiler-2023
北航 2023 秋编译技术实验课的代码。一个 SysY 语言（C 语言子集）的编译器，目标代码为 MIPS，使用 LLVM 作为中间代码。

> 从语义分析开始就变得十分丑陋。未来可能会考虑重写。

---
- **pack.sh**：将源程序打包，方便提交
- **run_llvm_ir.sh**：解释执行 LLVM 文件。例：`bash run_llvm_ir.sh llvm_ir.ll`
