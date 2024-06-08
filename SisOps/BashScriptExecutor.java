import java.io.IOException;

public class BashScriptExecutor extends ScriptExecutor {
    public BashScriptExecutor(String scriptPath) throws IOException {
        super(Runtime.getRuntime().exec(new String[]{"./"+ scriptPath}));
    }
}
