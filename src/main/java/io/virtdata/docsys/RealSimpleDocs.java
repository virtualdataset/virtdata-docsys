package io.virtdata.docsys;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class RealSimpleDocs {

    public static void main(String[] args) {
        if (args.length==0) {
            showHelp();
        }
        String subcmd = args[0].toLowerCase();
        args=Arrays.copyOfRange(args, 1, args.length);

        if (subcmd.equals("topics")) {
            listTopics();
        } else if (subcmd.equals("search")) {
            search(args);
        } else if (subcmd.equals("help")) {
            showHelp(Arrays.copyOfRange(args, 1, args.length - 1));
        } else if (subcmd.equals("server")) {
            runServer(args);
        }
    }

    private static void runServer(String[] serverArgs) {
        Path contentRoot = Paths.get("src/test/resources/testsite1/").toAbsolutePath().normalize();

        DocServer server = new DocServer(contentRoot);
        server.run();
    }

    private static void showHelp(String... helpArgs) {
    }

    private static void search(String[] searchArgs) {
    }

    private static void listTopics() {

    }
}
