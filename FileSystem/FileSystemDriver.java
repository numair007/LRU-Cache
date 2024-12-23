package FileSystem;

import java.util.*;

public class FileSystemDriver {
    
    public static void main(String[] args) {
        FileSystem fileSystem = new FileSystem();
        fileSystem.mkdir("/a/b/c/d");
        fileSystem.mkdir("/a/b/c/e");
        fileSystem.mkdir("/a/b/c/f/g");
        fileSystem.delete("/a/b/c/f/g");
        List<String> res = fileSystem.ls("/a/b/c");
        System.out.println(res);
    }
}

interface FileSystemInterface{
    public void mkdir(String path);
    public List<String> ls(String path);
    public void addContentToFile(String path, String  content);
    public String readContentFromFile(String path);
    public void delete(String path);
}



class Node{
    boolean isFile;
    TreeMap<String, Node> child;
    StringBuilder content;
    
    Node(){
        isFile = false;
        child = new TreeMap<>();
        content = null;
    }
    public void setFile(){
        this.isFile = true;
        content = new StringBuilder();
    }
    
    public void appendFile(String str){
        content.append(str);
    }
}

class FileSystem implements FileSystemInterface {

    Node root;
    public FileSystem() {
        root = new Node();
    }
    
    public List<String> ls(String path) {
         Node tem = root;
        String[] paths = path.split("/");
       // System.out.println("sdfv "+paths.length );
        List<String> result = new ArrayList<>();
        if(paths.length == 0){
            
            return new ArrayList<>(root.child.keySet());
        }
        ls(tem, paths,1, result, false);
        return result;
    }
    private void ls(Node currRoot, String[] paths, int i, List<String> result, boolean isReadFile){
        if(i >= paths.length) return;
        
        TreeMap<String, Node> map = currRoot.child;
        String next = paths[i];
        Node node = map.get(next);
        if(i == (paths.length-1)){
            if(node.isFile){
                if(isReadFile){
                    result.add(node.content.toString());
                }
                else
                    result.add(next);
            }
            else{
                result.addAll(new ArrayList<>(node.child.keySet()));
            }
            return;
        }
        else
         ls(map.get(next), paths, i+1, result, isReadFile);
    }
    
    public void mkdir(String path) {
        
        Node tem = root;
        String[] paths = path.split("/");
        mkdir(tem, paths,1, false, "");
        
    }
    private void mkdir(Node currRoot, String[] paths, int i, boolean isFile, String content){
        if(i >= paths.length) return;
        TreeMap<String, Node> map = currRoot.child;
        String next = paths[i];
        if(!map.containsKey(next)){
            map.put(next, new Node());
        }
        if(i == (paths.length-1) && isFile){
            Node node = map.get(next);
            if(node.isFile){
                
            }
            else{
                node.setFile();
                
            }
            
            node.appendFile(content);

            
        }
        mkdir(map.get(next), paths, i+1, isFile, content);
    }
    
    public void addContentToFile(String path, String content) {
        Node tem = root;
         String[] paths = path.split("/");
        mkdir(tem, paths, 1, true, content);
    }
    
    public String readContentFromFile(String path) {
        Node tem = root;
        String[] paths = path.split("/");
        List<String> result = new ArrayList<>();
        ls(tem, paths,1, result, true);
        return result.get(0);
    }

    private void delete(Node currNode, String[] paths, int i){
        if(i>=paths.length) return;

        String next = paths[i];
        Node nextNode = currNode.child.get(next);
        if(i == (paths.length-1)){
            currNode.child.remove(next);
            return;
        }
        else 
            delete(nextNode, paths, i+1);
    }
    public void delete(String path){
        Node tem = root;
        String[] paths = path.split("/");
        if(paths.length == 0){
            tem.child.clear();
            return;
        }
        delete(tem, paths, 1);
    }
}

