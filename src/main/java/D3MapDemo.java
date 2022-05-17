import io.helidon.common.http.MediaType;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.oracle.truffle.js.runtime.JSContextOptions.COMMONJS_REQUIRE_CWD_NAME;
import static com.oracle.truffle.js.runtime.JSContextOptions.COMMONJS_REQUIRE_NAME;

public class D3MapDemo {

    private static final int SERVER_PORT = 8000;
    private static String DEMO_ROOT;

    static {
        try {
            DEMO_ROOT = D3MapDemo.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        Source code = Source.newBuilder("js",
                        "const mapBuilder = require('" + DEMO_ROOT + "script.js');" +  //
                                "function createMap(data) {" + //
                                "  return mapBuilder.getMap(data); " + //
                                "};",
                        "code.js")
                .build();

        ThreadLocal<Value> functions = ThreadLocal.withInitial(() -> {
            Context context = Context.newBuilder("js")
                    .allowAllAccess(true)
                    .allowExperimentalOptions(true)
                    .options(setOptions())
                    .allowIO(true)
                    .build();
            context.eval(code);
            return context.getBindings("js").getMember("createMap");
        });


        Map<String, String> cache = new ConcurrentHashMap<>();
        var routing = Routing.builder().get("/ampel/{year}/{month}/{day}", (request, response) -> {
            var year = request.path().param("year");
            var month = request.path().param("month");
            var day = request.path().param("day");
            try {
                String image;
                if (cache.containsKey(year+month+day)) {
                    image = cache.get(year+month+day);
                } else {
                    var fun = functions.get();
                    Value img = fun.execute(day+"."+month+"."+year);
                    StringBuilder imageBuilder = new StringBuilder();
                    Consumer<Object> javaThen = imageBuilder::append;
                    img.invokeMember("then", javaThen);
                    image = imageBuilder.toString();
                    cache.put(year+month+day, image);
                }
                response.headers().contentType(MediaType.TEXT_HTML);
                response.status(200);
                String html = "<!DOCTYPE html>" + "<html><body>" +
                        "<h1>Corona Ampel fuer den " +
                        day + "." + month + "." + year + "</h1>" + image +
                        "</body></html>";
                response.send(html);
            } catch (Throwable e) {
                response.send(e.toString());
            }
        }).get("/",(request,response)->{
            try {
                response.headers().contentType(MediaType.TEXT_HTML);
                response.status(200);
                String html = "<!DOCTYPE html>" + "<html><body>" +
                        "<h1>NPM module loading demo</h1></body></html>";
                response.send(html);
            } catch (Throwable e) {
                response.send(e.toString());
            }
        }).build();
        var webServer = WebServer.builder().port(SERVER_PORT)
                .routing(routing)
                .build();
        webServer.start();
        System.out.println("INFO: D3 Map server started at: http://localhost:" + SERVER_PORT + "\n");
    }

    private static HashMap<String, String> setOptions() {
        HashMap<String, String> options = new HashMap<>();
        options.put(COMMONJS_REQUIRE_NAME, "true");
        options.put(COMMONJS_REQUIRE_CWD_NAME, DEMO_ROOT);
        options.put("js.global-property", "true");
        options.put("engine.WarnInterpreterOnly", "false");
        options.put("js.top-level-await", "true");
        options.put("js.commonjs-core-modules-replacements",
                        "fs:./emptyModule/," +
                        "net:../emptyModule/," +
                        "vm:vm-browserify/," +
                        "os:../emptyModule/," +
                        "tls:../emptyModule/," +
                        "child_process:../emptyModule/," +
                        "https:https-browserify/," +
                        "inherits:./node_modules/inherits/inherits_browser.js," +
                        "http:stream-http/," +
                        "stream:stream-browserify/," +
                        "path:path-browserify/," +
                        "tty:tty-browserify/," +
                        "zlib:browserify-zlib/," +
                        "crypto:crypto-browserify");
        return options;
    }

}
