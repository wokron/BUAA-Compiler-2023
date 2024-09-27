# BUAA-Compiler-2023
北航 2023 秋编译技术实验课的代码。一个 SysY 语言（C 语言子集）的编译器，目标代码为 MIPS，使用 LLVM 作为中间代码。

> 从语义分析开始就变得十分丑陋。未来可能会考虑重写。

---
- **pack.sh**：将源程序打包，方便提交
- **run_llvm_ir.sh**：解释执行 LLVM 文件。例：`bash run_llvm_ir.sh llvm_ir.ll`

---
**2024.09.02**：这个项目写得不好，如果希望参考的话可以考虑本人参与编译大赛时所写的编译器：[sysyc](https://github.com/wokron/sysyc)。

**2024.09.27**：本项目和 sysyc 以及 tolangc 包含在实验查重范围内。**不要抄袭**，尤其是不要抄助教的。
