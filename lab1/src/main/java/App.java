import org.jacop.constraints.Alldiff;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.Sum;
import org.jacop.constraints.XmulYeqZ;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.PrintOutListener;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;

public class App {

    public static void main(String[] args) {

        int n = 9;
        // int n_prefs = 17;
        // int[][] prefs = {{1,3}, {1,5}, {1,8},
        //     {2,5}, {2,9}, {3,4}, {3,5}, {4,1},
        //     {4,5}, {5,6}, {5,1}, {6,1}, {6,9},
        //     {7,3}, {7,8}, {8,9}, {8,7}};

        int[][] prefs = {{1, 3}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}, {9, 4}};

        int maxDiff = 1;

        Store store = new Store();

        // Create all variables
        IntVar[] vars = new IntVar[n];
        for(int i = 0; i < n; i++) {
            vars[i] = new IntVar(store, "var:"+Integer.toString(i), 1, n);
        }

        // Define constraints
        store.impose(new Alldiff(vars));

        // for(int[] pref : prefs) {
        //     int from = pref[0];
        //     int to = pref[1];
        //     IntVar dist = new IntVar(store, 0, maxDiff);
        //     Constraint csr = new Distance(vars[from-1], vars[to-1], dist);
        //     store.impose(csr);
        // }

        // Define cost function
        // Define one cost per preference
        IntVar[] costs = new IntVar[prefs.length];
        for(int i = 0; i < prefs.length; i++) {
            // Create cost variable
            costs[i] = new IntVar(store, 0, 1);

            // Get variables. Indexes must be subtracted
            int[] pref = prefs[i];
            int fromIndex = pref[0]-1;
            int toIndex = pref[1]-1;
            IntVar from = vars[fromIndex];
            IntVar to = vars[toIndex];
            // Define value of cost variable for this preference
            int fromValue = from.value();
            int toValue = to.value();
            int diff = Math.abs(fromValue - toValue);

            System.out.println(java.util.Arrays.toString(pref));
            System.out.println(fromValue);
            System.out.println(toValue);
            System.out.println();

            IntVar one = new IntVar(store, 1, 1);
            IntVar zero = new IntVar(store, 0, 0);
            // If we break preference
            if(diff > maxDiff) {
                store.impose( new XmulYeqZ(one, one, costs[i]) );
            } 
            else {
                store.impose( new XmulYeqZ(one, zero, costs[i]) );
            }
        }

        IntVar cost = new IntVar(store, "cost", 0, prefs.length);
        Constraint constConstr = new Sum(costs, cost);
        store.impose(constConstr);

        // Try finding solution witht depth first search algorithm
        Search<IntVar> search = new DepthFirstSearch<IntVar>();

        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(
            vars, 
            new SmallestDomain<IntVar>(), 
            new IndomainMin<IntVar>()
        );

        search.setSolutionListener(new PrintOutListener<IntVar>());

        boolean result = search.labeling(store, select, cost);

        if(result) {
            System.out.println("\n*** Yes!");
            System.out.println("Solution : "+ java.util.Arrays.asList(vars));

        } else {
            System.out.println("NOOOO! ");
        }
    }
}

