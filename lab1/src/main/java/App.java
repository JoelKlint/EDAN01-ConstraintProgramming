import org.jacop.core.*;
import org.jacop.constraints.*;
import org.jacop.search.*;

public class App {

    public static void main(String[] args) {

        int n = 9;
        // int n_prefs = 17;
        int[][] prefs = {{1,3}, {1,5}, {1,8},
            {2,5}, {2,9}, {3,4}, {3,5}, {4,1},
            {4,5}, {5,6}, {5,1}, {6,1}, {6,9},
            {7,3}, {7,8}, {8,9}, {8,7}};

        // int[][] prefs = {{1, 3}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}};

        int maxDiff = 1;

        Store store = new Store();

        // Create all variables
        IntVar[] vars = new IntVar[n];
        for(int i = 1; i <= n; i++) {
            vars[i-1] = new IntVar(store, Integer.toString(i), 1, n);
        }

        

        // Define constraints
        store.impose(new Alldiff(vars));

        for(int[] pref : prefs) {
            int from = pref[0];
            int to = pref[1];
            IntVar dist = new IntVar(store, 0, maxDiff);
            Constraint csr = new Distance(vars[from-1], vars[to-1], dist);
            store.impose(csr);
        }


        // Try finding solution witht depth first search algorithm
        Search<IntVar> search = new DepthFirstSearch<IntVar>();

        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(
            vars, new SmallestDomain<IntVar>(), new IndomainMin<IntVar>());

        search.setSolutionListener(new PrintOutListener<IntVar>());

        boolean result = search.labeling(store, select);
        boolean result2 = store.consistency();

        if(result) {
            System.out.println("\n*** Yes!");
            System.out.println("Solution : "+ java.util.Arrays.asList(vars));

        } else {
            System.out.println("NOOOO! ");
        }
    }
}

