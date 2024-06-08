import java.io.IOException;

public class PowerShellScriptExecutor extends ScriptExecutor {
    public PowerShellScriptExecutor(String scriptPath) throws IOException {
        super(Runtime.getRuntime().exec("powershell.exe -ExecutionPolicy Bypass -File \"" + scriptPath + "\""));
    }
}
