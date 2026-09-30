package dev.agentj.cli;

import picocli.CommandLine;
import java.nio.file.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name="agentj", mixinStandardHelpOptions=true, version="AgentJ 0.1.0", description="Build and inspect Java AI agents.")
public final class AgentJCli implements Callable<Integer> {
    @CommandLine.Command(name="doctor", description="Check the local Java/AgentJ environment.")
    int doctor() {
        System.out.println("AgentJ 0.1.0");
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("OS:   " + System.getProperty("os.name"));
        System.out.println("OK: core runtime available");
        return 0;
    }
    @CommandLine.Command(name="init", description="Create a starter AgentJ project in the current directory.")
    int init() throws Exception {
        Path root = Path.of("agentj-project"); Files.createDirectories(root.resolve("src/main/java"));
        Files.writeString(root.resolve("README.md"), "# AgentJ project\\n\\nSee https://github.com/agentj-dev/agentj for the runtime.\\n");
        System.out.println("Created " + root.toAbsolutePath());
        return 0;
    }
    public Integer call() { return 0; }
    public static void main(String[] args) { System.exit(new CommandLine(new AgentJCli()).execute(args)); }
}
