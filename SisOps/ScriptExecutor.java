public class ScriptExecutor extends Thread {
    Process process;

    public ScriptExecutor(Process process) {
        this.process = process;
    }

    public void stopScript() {
        process.destroy();
    }
}