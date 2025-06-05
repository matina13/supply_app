package org.example.demo;

import org.example.demo.structs.Supplier;
import org.example.demo.structs.SupplierMaterialInfo;
import org.example.demo.supplyChain.DataGetter;
import org.example.demo.supplyChain.Transaction.TravelTimeCalculator;

import java.util.*;

public class Algorithm {
    private int user_id;
    private DataGetter dataGetter;
    private ArrayList<Integer> wantedProducableGoodId;

    public Algorithm(int user_id, DataGetter dataGetter, ArrayList<Integer> wantedProducableGoodIds) {
        this.user_id = user_id;
        this.dataGetter = dataGetter;
        this.wantedProducableGoodId = wantedProducableGoodIds;
    }

    public ArrayList<HashMap<Integer, Integer>> start() {
        ArrayList<HashMap<Integer, Integer>> bestCalculation = new ArrayList<HashMap<Integer, Integer>>();
        for (int i : this.wantedProducableGoodId) {
            bestCalculation.add(calculate(i));
        }
        return bestCalculation;
    }

    private HashMap<Integer, Integer> calculate(int wantedProducableGoodId) {
        ArrayList<SupplierMaterialInfo> cheapestMaterialSellers = getCheapestMaterialSellers(wantedProducableGoodId);
        Sorter fastestDeliveryingSuppliers = calculateDays(this.user_id);
        return calculateBest(wantedProducableGoodId, cheapestMaterialSellers, fastestDeliveryingSuppliers);
    }

    private ArrayList<SupplierMaterialInfo> getCheapestMaterialSellers(int material_id) { //make if supplier has enough amount to supply for total wanted produced
        ArrayList<SupplierMaterialInfo> suppliersWithMaterials = this.dataGetter.getSuppliersThatSellMaterial(material_id);

        Collections.sort(suppliersWithMaterials, new Comparator<SupplierMaterialInfo>() {
            @Override
            public int compare(SupplierMaterialInfo smi1, SupplierMaterialInfo smi2) {
                return Integer.compare(smi1.getPrice(), smi2.getPrice());
            }
        });

        return suppliersWithMaterials;
    }

    private Sorter calculateDays(int user_id) {
        ArrayList<Supplier> suppliers = this.dataGetter.getSuppliers();
        int[] userLocation = this.dataGetter.getCurrentUserLocation(user_id);

        ArrayList<SupplierDistanceFromBuyer> supDistFromBuyer = new ArrayList<SupplierDistanceFromBuyer>();

        for (Supplier s : suppliers) {
            int distance = TravelTimeCalculator.calculate(userLocation[0], userLocation[1], s.getLocation_x(), s.getLocation_y());
            supDistFromBuyer.add(new SupplierDistanceFromBuyer(s.getSupplier_id(), distance));
        }

        Sorter fastestDeliveryingSuppliers = new Sorter(supDistFromBuyer, userLocation);
        fastestDeliveryingSuppliers.sort();

        return fastestDeliveryingSuppliers;
    }

    private HashMap<Integer, Integer> calculateBest(int material_id, ArrayList<SupplierMaterialInfo> cheapestMaterialSellers, Sorter fastestDeliveryingSuppliers) {
        ArrayList<Qualifier> qualifierList = new ArrayList<Qualifier>();
        for (int i = 0; i < cheapestMaterialSellers.size(); i++) {
            int qualifier = cheapestMaterialSellers.get(i).getPrice() * fastestDeliveryingSuppliers.get(i).getDistance();
            qualifierList.add(new Qualifier(cheapestMaterialSellers.get(i).getSupplier_id(), fastestDeliveryingSuppliers.get(i).getSupplier_id(), qualifier));
        }

        ArrayList<Qualifier> qualified = new ArrayList<Qualifier>(); //change names l8r

        for (Qualifier q : qualifierList) {
            if (q.getSupplier_id_1() == q.getSupplier_id_2()) {
                qualified.add(q);
            }
        }

        Collections.sort(qualified, new Comparator<Qualifier>() {
            @Override
            public int compare(Qualifier q1, Qualifier q2) {
                return Integer.compare(q1.getQualifier(), q2.getQualifier());
            }
        });

        if (qualified.isEmpty()) {
            SupplierMaterialInfo cheapestSupplier = cheapestMaterialSellers.get(0);
            qualified.add(new Qualifier(cheapestSupplier.getSupplier_id(), cheapestSupplier.getSupplier_id(), 0));
        }

        HashMap<Integer, Integer> best = new HashMap<Integer, Integer>();
        best.put(material_id, qualified.get(0).getSupplier_id_1());
        return best;
    }

    private class Sorter {
        private ArrayList<SupplierDistanceFromBuyer> array;
        private HashMap<Integer,Integer> indexMap = new HashMap<Integer,Integer>(); //links our index with the tracked array's Index
        private int[] userLocation;

        public Sorter(ArrayList<SupplierDistanceFromBuyer> arrayList, int[] userLocation) {
            this.array = arrayList;
            this.userLocation = userLocation;
        }

        private int indexTranslator(int idx) {
            int arrIdx = this.indexMap.get(idx);
            return arrIdx;
        }

        public SupplierDistanceFromBuyer get(int index) {
            return this.array.get(indexTranslator(index));
        }

        public void sort() {
            boolean done = false;
            for (int z = 0; z < this.array.size(); z++) { //initialize indexMap
                this.indexMap.put(z, z);
            }
            int counter = 0;
            while (!done) {
                for (int i = 0; i < this.array.size() - 1; i++) {
                    Integer first = this.array.get(indexTranslator(i)).getDistance();
                    Integer second = this.array.get(indexTranslator(i + 1)).getDistance();
                    int compareValue = first.compareTo(second);

                    if (compareValue > 0) {
                        int buf1 = this.indexMap.get(i);
                        int buf2 = this.indexMap.get((i + 1));
                        this.indexMap.replace(i,buf2);
                        this.indexMap.replace(i + 1,buf1);
                        counter = 0;
                    }
                    else if (compareValue < 0){counter = counter + 1;}
                    if (counter == this.array.size()) done = true;
                }
            }
        }

    }

    private class SupplierDistanceFromBuyer {
        private int supplier_id;
        private int distance;

        public SupplierDistanceFromBuyer(int supplier_id, int distance) {
            this.supplier_id = supplier_id;
            this.distance = distance;
        }

        public int getSupplier_id() {
            return this.supplier_id;
        }

        public int getDistance() {
            return this.distance;
        }
    }

    private class Qualifier {
        private int supplier_id_1;
        private int supplier_id_2;
        private int qualifier;

        public Qualifier(int supplier_id_1, int supplier_id_2, int qualifier) {
            this.supplier_id_1 = supplier_id_1;
            this.supplier_id_2 = supplier_id_2;
            this.qualifier = qualifier;
        }

        public int getSupplier_id_1() {
            return this.supplier_id_1;
        }

        public int getSupplier_id_2() {
            return this.supplier_id_2;
        }

        public int getQualifier() {
            return this.qualifier;
        }
    }

}