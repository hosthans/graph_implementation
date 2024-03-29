package com.hosthans.Algorithms.Wasserleitung;

import com.hosthans.Graph.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MaxFlow {

    final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    final String ANSI_BLACK = "\u001B[30m";
    final String ANSI_RED_BACKGROUND = "\u001B[41m";



    Graph_NOINPUT graph;
    Vertex Quelle;
    Vertex Senke;
    Graph_NOINPUT residual;


    int maxFlow = 0;
    int needed;

    public MaxFlow(String Quelle, String Senke, Graph_NOINPUT graph, int needed) throws IOException {
        this.graph = graph;
        this.Quelle = this.graph.getKnoten().get(Quelle);
        this.Senke = this.graph.getKnoten().get(Senke);
        this.residual = graph;
        this.needed = needed;
        initialize();
        ford();
        getErgebnis(this.needed);
    }

    public MaxFlow(Vertex Quelle, Vertex Senke, Graph_NOINPUT graph, int needed) throws IOException {
        this.graph = graph;
        this.Quelle = Quelle;
        this.Senke = Senke;
        this.residual = graph;
        this.needed = needed;
        initialize();
        ford();
        getErgebnis(this.needed);
    }

    //erst initialisieren (hier mit residualgraph)
    public void initialize() throws IOException {
        for (Vertex v : graph.graph.keySet()){
            for (Node n : graph.graph.get(v)){
                if (n.getE().weight != 0){
                    this.residual.addReverseEdge(n.getE().dest, v);
                }
            }
        }

        System.out.println(residual.printGraph());

    }

    //nachdem PFad existiert --> erstellen
    public LinkedList<Vertex> PfadErstellenBreitensuche(Vertex Quelle, Vertex Senke){
        HashSet<Vertex> visited = new HashSet<>();
        LinkedList<Vertex> queue = new LinkedList<>();
        LinkedList<NodeWParent> traversed = new LinkedList<>();

        visited.add(Quelle);
        queue.addLast(Quelle);
        traversed.add(new NodeWParent(Quelle));

        while (!queue.isEmpty()){
            Vertex current = queue.poll();

            for (Node n : residual.getNeighbors(current)){
                if ((!visited.contains(n.dest)) && (n.getE().getWeight() > 0)){
                    if (n.getDest().equals(Senke)){
                        traversed.add(new NodeWParent(n.getDest()));
                        traversed.getLast().setParent(current);
                        queue.clear();
                        break;
                    }
                    queue.addLast(n.getDest());
                    visited.add(n.getDest());
                    traversed.add(new NodeWParent(n.getDest()));
                    traversed.getLast().setParent(current);
                }
            }
        }

        return pfaduebergabe(traversed, Quelle, Senke);
    }

    public LinkedList<Vertex> pfaduebergabe(LinkedList<NodeWParent> currentList, Vertex Quelle, Vertex Senke){
        LinkedList<NodeWParent> list = new LinkedList<>();
        NodeWParent helper = null;
        for (NodeWParent node : currentList){
            if (node.getVertex().equals(Senke)){
                list.add(node);
            } else if (node.getVertex().equals(Quelle)){
                helper = node;
            }
        }

        while (!list.getLast().getParent().equals(Quelle)){
            for (NodeWParent node : currentList){
                if (node.getParent() != null){
                    if (list.getLast().getParent().equals(node.getVertex())){
                        list.add(node);
                    }
                }
            }

        }
        list.add(helper);

        LinkedList<Vertex> listRueckgabe = new LinkedList<>();
        for (NodeWParent node : list){
            listRueckgabe.addFirst(node.getVertex());
        }
        return listRueckgabe;
    }

    public boolean PfadExistent(Vertex Quelle, Vertex Senke){
        HashSet<Vertex> visited = new HashSet<>();
        LinkedList<Vertex> queue = new LinkedList<>();
        LinkedList<NodeWParent> traversed = new LinkedList<>();

        visited.add(Quelle);
        queue.addLast(Quelle);
        traversed.add(new NodeWParent(Quelle));

        while (!queue.isEmpty()){
            Vertex current = queue.poll();

            for (Node n : residual.getNeighbors(current)){
                if ((!visited.contains(n.dest)) && (n.getE().getWeight() > 0)){
                    //wenn Knoten gleich Endknoten
                    if (n.dest.equals(Senke)){
                        traversed.add(new NodeWParent(n.dest));
                        traversed.getLast().setParent(current);
                        return true;
                    }
                    //Knoten == gesehen --> wird hinzugefügt um diesen in der Liste vorzufinden
                    queue.addLast(n.getDest());
                    visited.add(n.getDest());
                    traversed.add(new NodeWParent(n.getDest()));
                    traversed.getLast().setParent(current);
                }
            }
        }
        return false;
    }

    public int ford() throws IOException {

        while (PfadExistent(Quelle, Senke)){
            int flow = Integer.MAX_VALUE;
            LinkedList<Vertex> pfad = PfadErstellenBreitensuche(this.Quelle, this.Senke);
            int counter = 0;

            for (Vertex v : pfad) {
                LinkedList<Node> nodes = (LinkedList<Node>) residual.getNeighbors(v);
                // wir gehen alle knoten des paths bis auf den endknoten ab und speichern seine nachbarn
                // dann müssen wir den destination-Vertex vom aktuellen knoten vom path in der nodes liste finden
                // dann weisen wir restcapacity die minimale kantengewichtung zu
                //System.out.println(v + " - " + nodes);
                Vertex destVertex = pfad.get((pfad.indexOf(v)) + 1);
                for (Node n : nodes) {
                    if (n.getDest() == destVertex) {
                        if (n.getE().getWeight() < flow) {
                            flow = n.getE().getWeight();
                        }
                    }
                }
                // counter, damit für die Senke / Endknoten nicht mehr nach dem nachbar geschaut wird.
                counter++;
                if (counter == pfad.size()-1) {
                    // counter zurücksetzen auf 0
                    counter = 0;
                    break;
                }
            }

            for (Vertex v : pfad) {
                LinkedList<Node> nodes = (LinkedList<Node>) residual.getNeighbors(v);
                Vertex destVertex = pfad.get((pfad.indexOf(v)) + 1);
                for (Node n : nodes) {
                    if (n.getDest() == destVertex) {
                        n.getE().weight = n.getE().weight - flow;
                    }
                }

                //System.out.println(v + " - " + nodes);

                counter++;
                if (counter == pfad.size()-1) {
                    break;
                }
            }


            for (int i = pfad.size()-1; i > 0; i--) {

                Vertex v = pfad.get(i);

                // nachbarn von v
                LinkedList<Node> nodes = (LinkedList<Node>) residual.getNeighbors(v);
                Vertex destVertex = pfad.get((pfad.indexOf(v)) - 1);

                for (Node n : nodes) {
                    if (n.getDest() == destVertex) {
                        n.getE().weight = n.getE().weight + flow;
                    }
                }




            }

            this.maxFlow = this.maxFlow + flow;

        }

        System.out.println(this.maxFlow);
        return this.maxFlow;
    }

    public boolean GenugPlatz(int needed){
        if (this.maxFlow >= needed){
            return true;
        }else {
            return false;
        }
    }

    public void getErgebnis(int i){

        if (!GenugPlatz(i)){
            System.out.print(ANSI_RED_BACKGROUND + ANSI_BLACK + "Das Netz kann maximal " + this.maxFlow + " m^3 Wasser fördern (pro Stunde) ---- somit reicht es NICHT aus für " + i + "." );
        } else {
            System.out.print(ANSI_GREEN_BACKGROUND + ANSI_BLACK + "Das Netz kann maximal " + this.maxFlow + " m^3 Wasser fördern (pro Stunde) ---- somit reicht es aus für " + i + ".");
        }
    }

}
