import java.nio.charset.StandardCharsets;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ByteArrayResource;

public class YamlTry {
    public static void main(String[] args) throws Exception {
        String yml = "spring:\n  autoconfigure:\n    exclude: com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure\n  cloud:\n    refresh:\n      never-refreshable: datasource\n\nfeign:\n  sentinel:\n    enabled: true\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: '*'\n";
        ByteArrayResource resource = new ByteArrayResource(yml.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "DEFAULT_GROUP@application-dev.yml";
            }
        };
        try {
            System.out.println(new YamlPropertySourceLoader().load("n", resource));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
