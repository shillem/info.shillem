package info.shillem.util.dots;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    public String resolve(String[] args) throws ParseException {
        CommandLine cmd = parser.parse(options, args);

        return instructions
                .entrySet()
                .stream()
                .filter((e) -> cmd.hasOption(
                        Optional
                                .ofNullable(e.getValue().getLongOpt())
                                .orElse(e.getValue().getOpt())))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new UnrecognizedOptionException(cmd.getArgList().toString()));
    }

}
