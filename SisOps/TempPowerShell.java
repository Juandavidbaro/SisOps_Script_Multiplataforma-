import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TempPowerShell extends Thread {
    Process process;

    public TempPowerShell(String scriptPath, String args) throws IOException {
        process = Runtime.getRuntime().exec("powershell.exe -ExecutionPolicy Bypass -File \"" + scriptPath + "\"" + " " + args);
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
