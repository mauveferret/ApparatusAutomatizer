package ru.mauveferret;

public class TempPathFinder {

    String author;
    String terminal;
    String TMP;
    String GateControl;
    String angel;
    String Gauge;
    String Arduino;

    String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

    public TempPathFinder(boolean isWorkSpace) {
        if (isWorkSpace)
            author = "dgbulgadaryan.2133-20115";
        else
            author = "mauve";
        path = path.substring(0,path.indexOf("ApparatusAutomatizer")+"ApparatusAutomatizer".length());
        System.out.println(path);
        //FIXME
        path = path.replaceAll("[/]","[\\]");
        terminal = path+"\\resources\\terminal";
        System.out.println("se_ "+terminal);
        TMP = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\TMP";
        GateControl = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\gate";
        angel = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\angel";
        Gauge = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\Gauge";
        Arduino = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\Arduino";


        System.out.println("___ "+path);
    }

/*
 String myJarPath = RootFxmlController.class.getProtectionDomain().getCodeSource().getLocation().getPath();
           String dirPath = new File(myJarPath).getParent();
           FileChooser.ExtensionFilter ScatterFilter =
                   new FileChooser.ExtensionFilter("SCATTER/TRIM (.dat, .txt)", "*.dat","*.txt");
           fileChooser.setTitle("Необходимо выбрать файл SC******.dat (или *.txt в случае TRIM)");
           fileChooser.getExtensionFilters().add(ScatterFilter);
           fileChooser.setSelectedExtensionFilter(ScatterFilter);
           fileChooser.setInitialDirectory(new File(dirPath));
           }
       else
           //look file in previous directory
           fileChooser.setInitialDirectory(new File(path.substring(0, path.lastIndexOf("\\"))));
       File  file = fileChooser.showOpenDialog(button.getScene().getWindow());
       path = file.getAbsolutePath();

        if (file.getPath().contains("SC")) FileType.setValue(0);
        else
       if (file.getPath().contains(".txt"))
       {
           try {

               FileType.setValue(100);
               BufferedReader br = new BufferedReader(new FileReader(path));
               //rubbish lines
               String line = br.readLine();
               while (!line.contains("TRIM Calc.")) line = br.readL
 */

}
