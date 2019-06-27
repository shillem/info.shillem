package info.shillem.util.dots;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

public class Commands {

    public class Result {

        private final String name;
        private final String selector;
        private final CommandLine cmd;

        private Result(String name, String selector, CommandLine cmd) {
            this.name = name;
            this.selector = selector;
            this.cmd = cmd;
        }

        public CommandLine getCommandLine() {
            return cmd;
        }

        public String getTriggeredCommandName() {
            return name;
        }

        public List<String> getTriggeredCommandValues() {
            return Optional
                    .ofNullable(cmd.getOptionValues(selector))
                    .map(Arrays::asList)
                    .orElse(Collections.emptyList());
        }

    }

    private final String title;
    private final Map<String, Option> instructions;
    private final CommandLineParser parser;
    private final Options options;

    public Commands(String title, Map<String, Option> instructions) {
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.instructions = Objects.requireNonNull(instructions, "Instructions cannot be null");
        this.parser = new DefaultParser();

        this.options = new Options();
        this.instructions.values().forEach(this.options::addOption);
    }

    public String getHelp() {
        HelpFormatter formatter = new HelpFormatter();

        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);

        formatter.printHelp(
                writer,
                HelpFormatter.DEFAULT_WIDTH,
                title,
                null,
                options,
                HelpFormatter.DEFAULT_LEFT_PAD,
                20,
                null);

        return out.toString();
    }

    public Result resolve(String[] args) throws ParseException {
        CommandLine cmd = parser.parse(options, args);

        for (Map.Entry<String, Option> entry : instructions.entrySet()) {
            Option opt = entry.getValue();
            String selector = Optional
                    .ofNullable(opt.getLongOpt())
                    .orElse(opt.getOpt());

            if (cmd.hasOption(selector)) {
                return new Result(entry.getKey(), selector, cmd);
            }
        }

        throw new UnrecognizedOptionException(cmd.getArgList().toString());
    }

}
