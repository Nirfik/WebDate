package utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FSUtils {

    private static FSUtils fsUtils;
    private Map<String, List<String>> myFS = new HashMap<>();
    public static final String HTMLDIR = "templates";
    public static final String signIn = "signIn.html";
    public static final String date = "date.html";
    private final Configuration CFG;


    private FSUtils(){
        CFG = new Configuration(Configuration.VERSION_2_3_20);
        CFG.setDefaultEncoding("UTF-8");
    }

    public static Map<String, String> file2Map(Path path) throws IOException {
        Map<String, String> result = new HashMap<>();
        Files.lines(path)
                .map(s -> s.replaceAll(" ", "").split("=", 2))
                .forEach(t -> result.put(t[0], t[1]));
        return result;
    }

    public synchronized static FSUtils getInstance(){
        return fsUtils == null ? fsUtils = new FSUtils() : fsUtils;
    }

    public String getFile(String filename, Map<String, Object> data) throws IOException {
        Writer stream = new StringWriter();
        Template template = CFG.getTemplate(HTMLDIR + "/" + filename);
        try {
            template.process(data, stream);
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return stream.toString();
    }

}
