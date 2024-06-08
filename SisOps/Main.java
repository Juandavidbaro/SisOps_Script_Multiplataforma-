import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private boolean powershell = System.getProperty("os.name").toLowerCase().contains("windows");
    private boolean bash = detectWsl() || !powershell;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, Double> memoryUsageMap = new HashMap<>();
    private ScriptExecutor cpuMonitor;
    private ScriptExecutor memoryMonitor;
    private Thread tempExecutor;

    public static boolean detectWsl() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            try {
                Process process = Runtime.getRuntime().exec(
                        "reg query HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\AppModel\\StateRepository\\Packages\\Microsoft-Windows-Subsystem-Linux");
                int exitCode = process.waitFor();
                return exitCode == 0; // Si el comando reg query tiene éxito, el WSL está instalado
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Main() throws IOException {
        if (powershell) {
            System.out.println("POWERSHELL detectado");
            this.cpuMonitor = new PowerShellScriptExecutor("topTenCpu.ps1");
            this.memoryMonitor = new PowerShellScriptExecutor("topTenMemory.ps1");
        } else {
            System.out.println("BASH detectado");
            this.cpuMonitor = new BashScriptExecutor("topTenCpu.sh");
            this.memoryMonitor = new BashScriptExecutor("topTenMemory.sh");
        }
    }

    public static void main(String[] args) {
        Main main;
        try {
            main = new Main();
            main.runMenu();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runMenu() {
        cpuMonitor.start();
        memoryMonitor.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            displayMenu();
            try {
                String choice = reader.readLine();
                switch (choice) {
                    case "1":
                        displayMemoryOptions();
                        String choiceMemory = reader.readLine();
                        switch (choiceMemory) {
                            case "1":
                                monitorMemoryTop10();
                                break;
                            case "2":
                                monitorMemory();
                                break;
                            case "3":
                                System.out.println("");
                                System.out.println("Ingresa el ID del proceso");
                                int pidString1 = Integer.parseInt(reader.readLine());
                                tempExecutor = bash ? new TempBash("memoryInterval.sh", pidString1 +"")
                                        : new TempPowerShell("./memoryInterval.ps1",
                                                "-pidSeleccionado " + pidString1);
                                tempExecutor.start();
                                break;
                            default:
                                System.out.println("Selecciona una opción valida");
                        }
                        break;
                    case "2":
                        displayCpuOptions();
                        String choiceCpu = reader.readLine();
                        switch (choiceCpu) {
                            case "1":
                                monitorCPU();
                                break;
                            case "2":
                                cpuGeneralState();
                                break;
                            case "3":
                                System.out.println("");
                                System.out.println("Ingresa el ID del proceso");
                                int pidString2 = Integer.parseInt(reader.readLine());
                                tempExecutor = bash ? new TempBash("cpuInterval.sh", pidString2+"")
                                        : new TempPowerShell("./cpuInterval.ps1",
                                                "-pidSeleccionado " + pidString2);
                                tempExecutor.start();
                                break;
                            case "4":
                                break;
                            default:
                                System.out.println("Operación invalida");
                        }
                        break;
                    case "3":
                        displayDiskOptions();
                        String choiceDisk = reader.readLine();
                        switch (choiceDisk) {
                            case "1":
                                System.out.println(
                                        "Digite una ruta que desee evaluar, si desea evaluar todo el sistema presione Enter");
                                String path = reader.readLine();
                                if (path.isEmpty()) {
                                    System.out.println("Calculando");
                                    monitorDisk();
                                } else {
                                    monitorDiskWithSpecificPath(path);
                                }
                                break;
                            case "2":
                                disksState();
                                break;
                            case "3":
                                System.out.println("");
                                System.out.println("Digite una ruta que desee evaluar, si desea evaluar todo el sistema presione Enter");
                                String pathString = reader.readLine();
                                tempExecutor = bash ? new TempBash("diskInterval.sh", pathString)
                                        : new TempPowerShell("./diskInterval.ps1",
                                                "-ruta " + pathString);
                                tempExecutor.start();
                                break;
                        }
                        break;
                    case "4":
                        cpuMonitor.stopScript();
                        memoryMonitor.stopScript();

                        executor.shutdown();
                        System.exit(0);
                    case "5":
                        BufferedReader br = new BufferedReader(new FileReader("./memoryLog.txt"));
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                        br.close();

                        br = new BufferedReader(new FileReader("./cpuLog.txt"));
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                        br.close();
                        break;

                    default:
                        System.out.println("Opción inválida");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayMenu() {
        System.out.println("Seleccione una opción de monitoreo:");
        System.out.println("1. Monitorear memoria");
        System.out.println("2. Monitorear CPU");
        System.out.println("3. Monitorear discos");
        System.out.println("4. Salir");
        System.out.println("5. Ver  archivos que se ejecutan en 2do plano: ");
        System.out.print("Ingrese su elección: ");
        
    }

    private void displayDiskOptions() {
        System.out.println("Seleccione una opción de monitoreo de disco:");
        System.out.println("1. Mostrar 10 archivos mas grandes");
        System.out.println("2. Estado general de los discos");
        System.out.println("3. Monitorear ruta por 5 minutos");
        System.out.println("4. Salir");
        System.out.print("Ingrese su elección: ");
    }

    private void displayCpuOptions() {
        System.out.println("Seleccione una opción de monitoreo de CPU:");
        System.out.println("1. Listado de top 10 de procesos con mayor uso de CPU");
        System.out.println("2. Estado General de la CPU (%usado kernel, % usado usuario, Idle)");
        System.out.println("3. Monitorear CPU por 5 minutos");
        System.out.println("4. Salir");
        System.out.print("Ingrese su elección: ");
    }

    private void displayMemoryOptions() {
        System.out.println("Seleccione una opción de monitoreo de memoria:");
        System.out.println("1. Listado de top 10 de procesos con mayor uso de memoria");
        System.out.println("2. Estado General de la memoria (cantidad, usada, disponible)");
        System.out.println("3. Monitorear memoria por 5 minutos");
        System.out.println("4. Salir");
        System.out.print("Ingrese su elección: ");
    }

    private void monitorMemoryTop10() {
        if (powershell) {

            executeCommand(new String[] { "powershell", "-Command",
                    "Get-Process | Sort-Object -Descending CPU | Select-Object -First 10" });

        }
        if (bash) {
            System.out.println("Executing Bash command to monitor memory...");
            executeCommand(new String[] { "/bin/bash", "-c",
                    "ps aux --sort=-%mem | head -n 11 | awk '{print $1,$2,$3,$4,$5,$6,$7,$8,$9}'" });

        }
    }

    private void monitorMemory() {
        if (powershell) {
            executeCommand(new String[] { "powershell", "-Command",
                    "Get-ComputerInfo -Property 'CsTotalPhysicalMemory','OsTotalVisibleMemorySize','OsFreePhysicalMemory'" });

        }
        if (bash) {
            System.out.println("Executing Bash command to monitor memory...");
            executeCommand(new String[] { "/bin/bash", "-c", "free -h" });

        }
    }

    private void monitorCPU() {
        if (powershell) {
            executeCommand(new String[] { "powershell", "-Command",
                    "Get-Process | Sort-Object -Descending CPU | Select-Object -First 10" });
            executeCommand(new String[] { "powershell", "-Command",
                    "Get-WmiObject -Class Win32_Processor | Select-Object LoadPercentage" });
        }
        if (bash) {
            executeCommand(new String[] { "/bin/bash", "-c",
                    "ps aux --sort=-%cpu | head -n 11| awk '{print $1,$2,$3,$4,$5,$6,$7,$8,$9}'" });
        }
    }

    private void cpuGeneralState() {
        if (powershell) {
            executeCommand(new String[] { "powershell", "-Command",
                    "Get-WmiObject -Class Win32_Processor | Select-Object Name, LoadPercentage | ForEach-Object { '{0}: {1}%' -f $_.Name, $_.LoadPercentage }" });
        }
        if (bash) {
            executeCommand(new String[] { "/bin/bash", "-c",
                    "mpstat 1 1 | awk 'NR==4{print \"User: \" $3 \"%, System: \" $5 \"%, Idle: \" $NF \"%\"}'" });
        }
    }

    private void monitorDisk() {
        if (powershell) {
            executeCommand(new String[] { "powershell", "-Command",
                    "Get-ChildItem -Path C:\\ -Recurse -File | Sort-Object Length -Descending | Select-Object -First 10" });
        }
        if (bash) {
            executeCommand(
                    new String[] { "/bin/bash", "-c", "find / -type f -exec du -h {} + | sort -rh | head -n 10" });
        }
    }

    private void disksState() {
        if (powershell) {
            executeCommand(new String[] { "powershell", "-Command", "Get-PSDrive -PSProvider FileSystem" });
        }
        if (bash) {
            executeCommand(new String[] { "/bin/bash", "-c", "df -h" });
        }
    }

    private void monitorDiskWithSpecificPath(String path) {
        if (powershell) {
            executeCommand(new String[] { "powershell", "-Command",
                    "Get-ChildItem -Path " + path
                            + " -Recurse -File | Sort-Object Length -Descending | Select-Object -First 10" });
        }
        if (bash) {
            executeCommand(new String[] { "/bin/bash", "-c",
                    "find " + path + " -type f -exec du -h {} + | sort -rh | head -n 10" });
        }
    }

    private void executeCommand(String[] command) {
        executeCommand(command, false);
    }

    private void executeCommand(String[] command, boolean convertToMB) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (convertToMB && (powershell || (line.matches(".*\\d+\\s+\\d+\\s+\\d+\\s+\\d+.*") && !powershell))) {
                    // Extract process name, ID, and RSS value, and convert to MB
                    if (powershell) {
                        String[] columns = line.trim().split("\\s+", 8);
                        if (columns.length >= 8 && columns[3].matches("\\d+")) {
                            String processName = columns[7];
                            String processId = columns[5];
                            int rssInKB = Integer.parseInt(columns[3]); // Assuming the 4th column is WS(K) in KB
                            double rssInMB = rssInKB / 1024.0;
                            String key = processName + " (PID: " + processId + ")";
                            memoryUsageMap.put(key, memoryUsageMap.getOrDefault(key, 0.0) + rssInMB);
                        }
                    } else {
                        String[] columns = line.trim().split("\\s+");
                        if (columns.length > 5) {
                            String processName = columns[10];
                            String processId = columns[1];
                            int rssInKB = Integer.parseInt(columns[5]); // Assuming the 6th column is RSS
                            double rssInMB = rssInKB / 1024.0;
                            String key = processName + " (PID: " + processId + ")";
                            memoryUsageMap.put(key, memoryUsageMap.getOrDefault(key, 0.0) + rssInMB);
                        }
                    }
                } else {
                    System.out.println(line);
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void monitorMemoryAsync() {
        executor.submit(() -> {
            try {
                for (int i = 0; i < 60; i++) { // 1 minuto (60 * 1000ms)
                    if (powershell) {
                        executeCommand(new String[] { "powershell", "-Command",
                                "Get-Process | Sort-Object -Descending WorkingSet | Select-Object -First 10 | Format-Table -AutoSize" },
                                true);
                    }
                    if (bash) {
                        executeCommand(new String[] { "/bin/bash", "-c", "ps aux --sort=-%mem | head -n 11" }, true);
                    }
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                // Print the accumulated memory usage
                printMemoryUsageMap();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void monitorProcess(String command, String reportFileName) {
        try (FileWriter writer = new FileWriter(reportFileName)) {
            long startTime = System.currentTimeMillis();
            long endTime = startTime + 5 * 60 * 1000;

            while (System.currentTimeMillis() < endTime) {
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\n");
                }
                reader.close();
                TimeUnit.MILLISECONDS.sleep(1000);
            }

            System.out.println("Monitoring completed. Report saved to " + reportFileName);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printMemoryUsageMap() {
        System.out.println("Acumulación de uso de memoria por proceso:");
        for (Map.Entry<String, Double> entry : memoryUsageMap.entrySet()) {
            System.out.printf("%s: %.2f MB\n", entry.getKey(), entry.getValue());
        }
    }
}
