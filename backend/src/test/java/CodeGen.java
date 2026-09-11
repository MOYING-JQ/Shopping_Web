import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import java.util.Collections;

public class CodeGen {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://127.0.0.1:3306/ecommerce?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai",
                        "root",
                        "zjq14551"
                )
                .globalConfig(builder -> {
                    builder.author("moying")
                            .outputDir(System.getProperty("user.dir") + "/src/main/java")
                            .disableOpenDir();
                })
                .packageConfig(builder -> {
                    builder.parent("com.moying.project_test")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            // ✅ 正确常量 OutputFile.xml
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    System.getProperty("user.dir") + "/src/main/resources/mapper"
                            ));
                })
                .strategyConfig(builder -> builder
                        .addInclude("t_order")
                        .addTablePrefix("t_")
                        .entityBuilder()
                        .naming(NamingStrategy.underline_to_camel)
                        .columnNaming(NamingStrategy.underline_to_camel)
                        .enableTableFieldAnnotation()
                        .enableLombok()
                )
                .execute();
    }
}



