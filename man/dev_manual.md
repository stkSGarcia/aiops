# Config
# Contributing Guide
- 不要使用`println`，使用Logger接口。
- 记录日志的时候通过参数化记录变量，比如：
- 尽量不要复制代码，而应该扩展。
- 通用方法先在Utils类中寻找。
- 依赖添加
- 无用的、废弃的代码请删除或写上注释和注解，以免给别人带来误解
- 单元测试中应该测实际的代码，一些和实际代码无关的测试请使用Scala console，比如测试正则。
- 单元测试的测试资源放在test resource文件夹下。
- NEVER return null
TODO
- plain模块更新后未测试
- log模块未更新
