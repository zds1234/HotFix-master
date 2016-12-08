package dodola.patch
import javassist.ClassPool
import javassist.CtClass

/**
 * 你在生成apk之前，就需要阻止相关类打上CLASS_ISPREVERIFIED的标志了让LoadBugClass在构造方法中，
 * 去引用别的dex文件，比如：hack.dex中的某个类即可。
 */
public class PatchClass {
    /**
     * 植入代码
     * @param buildDir 是项目的build class目录,就是我们需要注入的class所在地
     * @param lib 这个是hackdex的目录,就是AntilazyLoad类的class文件所在地
     */
    public static void process(String buildDir, String lib) {

        println(lib)
        ClassPool classes = ClassPool.getDefault()
        classes.appendClassPath(buildDir)
        classes.appendClassPath(lib)

        //下面的操作比较容易理解,在将需要关联的类的构造方法中插入引用代码
        CtClass c = classes.getCtClass("dodola.hotfix.BugClass")
        if (c.isFrozen()) {
            c.defrost()
        }
        println("====添加构造方法====")
        def constructor = c.getConstructors()[0];
        constructor.insertBefore("System.out.println(dodola.hackdex.AntilazyLoad.class);")
        c.writeFile(buildDir)


        CtClass c1 = classes.getCtClass("dodola.hotfix.LoadBugClass")
        if (c1.isFrozen()) {
            c1.defrost()
        }
        println("====添加构造方法====")
        def constructor1 = c1.getConstructors()[0];
        constructor1.insertBefore("System.out.println(dodola.hackdex.AntilazyLoad.class);")
        c1.writeFile(buildDir)


    }

    static void growl(String title, String message) {
        def proc = ["osascript", "-e", "display notification \"${message}\" with title \"${title}\""].execute()
        if (proc.waitFor() != 0) {
            println "[WARNING] ${proc.err.text.trim()}"
        }
    }
}
