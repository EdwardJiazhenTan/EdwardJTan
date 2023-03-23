// Import any package as required
import java.io.*;
import java.util.*;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.io.IOException;

public class HuffmanSubmit implements Huffman {

    private static class node {
        node left, right;
        char ch;
        int freq;

        public node(char ch, int freq) {
            this.ch = ch;
            this.freq = freq;
        }

        // check if an item is leaf(a character)
        public boolean isLeaf() {
            return left == null || right == null;
        }
    }

    // methods that compare two nodes based on frequency
    class MyComparator implements Comparator<node> {
        public int compare(node x, node y) {
            return x.freq - y.freq;
        }
    }

    public static void main(String[] args) throws IOException {

            Huffman huffman = new HuffmanSubmit();
            huffman.encode("ur.jpg", "ur.enc", "freq.txt");
            huffman.decode("ur.enc", "ur_dec1.jpg", "freq.txt");
        }


        // After decoding, both ur.jpg and ur_dec.jpg should be the same.
        // On linux and mac, you can use `diff' command to check if they are the same.

    // recursive call to find huffman code for a char
    public static void findCode(node root, String s, HashMap<Character, String> huffmanCode) {
        if (root == null)
            return;
        if (root.isLeaf()) {
            huffmanCode.put(root.ch, s);
            return;
        }
        findCode(root.left, s + "0", huffmanCode);
        findCode(root.right, s + "1", huffmanCode);
    }

    // convert string/char to its 8 bit binary representation
    public static String convertStringToBinary(String input) {
        StringBuilder sb = new StringBuilder();
        char[] chars = input.toCharArray();
        for (char c : chars) {
            sb.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return sb.toString();
    }

    @Override
    public void encode(String inputFile, String outputFile, String freqFile) {

        //list: list of characters
        //freq: list of frequency, ordered in same way as characters
        ArrayList<Character> list = new ArrayList<>();
        ArrayList<Character> outPutList = new ArrayList<>();
        ArrayList<Integer> freq = new ArrayList<>();
        HashMap<Character, String> dictionary = new HashMap<>();

        // load file
        BinaryIn in = new BinaryIn(inputFile);
        BinaryOut out = new BinaryOut(outputFile);

        // read file and count frequency
        while (!in.isEmpty()) {
            char c = in.readChar();
            outPutList.add(c);
            if (!list.contains(c)) {
                list.add(c);
                freq.add(1);
            } else {
                int index = list.indexOf(c);
                freq.set(index, freq.get(index) + 1);
            }
        }
        System.out.println("list: " + list);

        //sory arrays by character
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i) > list.get(j)) {
                    char temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                    int temp2 = freq.get(i);
                    freq.set(i, freq.get(j));
                    freq.set(j, temp2);
                }
            }
        }

        // build a heap of nodes(huffman tree)
        PriorityQueue<node> Nodes = new PriorityQueue<>(new MyComparator());
        for (int i = 0; i < list.size(); i++) {
            node hn = new node(list.get(i), freq.get(i));
            hn.left = null;
            hn.right = null;
            Nodes.add(hn);
        }
        node root = null;
        while (Nodes.size() > 1) {
            node x = Nodes.poll();
            node y = Nodes.poll();
            node f = new node('\0', x.freq + y.freq);
            f.left = x;
            f.right = y;
            root = f;
            Nodes.add(f);
        }

        // find huffman code for each character and assign them in a dictionary
        // freqFile is the file that stores the frequency of each character

        findCode(root, "", dictionary);

        // write the dictionary to freqFile

        try {
            FileWriter myWriter = new FileWriter(freqFile);
            for (int i = 0; i < list.size(); i++) {
                myWriter.write((convertStringToBinary(String.valueOf(list.get(i)))) + ":" + freq.get(i));
                myWriter.write("\r\n");
                // for test purpose
                System.out.println(list.get(i)+ ":"+convertStringToBinary(String.valueOf(list.get(i))) + ":" + freq.get(i) +"--" +dictionary.get(list.get(i)));
            }
            System.out.println("Successfully wrote to the file.");
            myWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred.");
        }

        //outputFile is the file that stores the encoded message
        for (int i = 0; i < outPutList.size(); i++) {
            out.write(dictionary.get(outPutList.get(i)));
        }
        out.close();
        int x = outPutList.size();
        System.out.println("size:" + x);
    }


    public String convertBinaryToString(String binary) {
        String[] binaryArray = binary.split("(?<=\\G.{8})");
        StringBuilder sb = new StringBuilder();
        for (String s : binaryArray) {
            int decimal = Integer.parseInt(s, 2);
            sb.append((char) decimal);
        }
        return sb.toString();
    }

    public int[] readFreqFile(String freqFileName) throws IOException {
        int[] freq = new int[256];
        BufferedReader br = new BufferedReader(new FileReader(freqFileName));
        String line;
        while (!((line = br.readLine()) == null)) {
            String[] line_split = line.split(":");
            char ch = (char) Integer.parseInt(line_split[0], 2);
            int counts = Integer.parseInt(line_split[1]);
            System.out.println("ch "+ ch + " " + counts);
            freq[ch] = counts;
        }
        br.close();
        return freq;
    }


    @Override
    public void decode(String inputFile, String outputFile, String freqFile){

        try {
            // read inputFile and decode it
            BinaryIn in = new BinaryIn(inputFile);
            BinaryOut out = new BinaryOut(outputFile);
            HashMap<Character, String> dictionary = new HashMap<>();

            int[] freq = new int[256];
            freq = readFreqFile(freqFile);
            System.out.println("freq: " + Arrays.toString(freq));

            // build a heap of nodes(huffman tree)
            PriorityQueue<node> Nodes = new PriorityQueue<>(new MyComparator());
            for (char i = 0; i < 256; i++) {
                if (freq[i] > 0) {
                    node hn = new node(i, freq[i]);
                    hn.left = null;
                    hn.right = null;
                    Nodes.add(hn);
                    System.out.println(i + " " + convertStringToBinary(String.valueOf(i)) + " " + freq[i]);
                }
            }
            node root = null;
            while (Nodes.size() > 1) {
                node x = Nodes.poll();
                node y = Nodes.poll();
                node f = new node('\0', x.freq + y.freq);
                f.left = x;
                f.right = y;
                root = f;
                Nodes.add(f);
            }
            findCode(root, "", dictionary);
            System.out.println("dictionary: " + dictionary);

            // decode the file
            int length = 0;
            for (int i = 0; i < freq.length; i++) {
                length += freq[i];
            }
            for (int i = 0; i < length; i++) {
                node current = root;
                while (!current.isLeaf() && !in.isEmpty()) {
                    if (in.readChar() == '0') {
                        current = current.left;
                    } else {
                        current = current.right;
                    }
                }
                out.write(current.ch);
                System.out.println("this: " + current.ch);
            }
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred.");
        }


    }

}




