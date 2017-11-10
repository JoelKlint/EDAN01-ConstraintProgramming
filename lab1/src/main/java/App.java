import org.jacop.constraints.Alldiff;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.Distance;
import org.jacop.constraints.Reified;
import org.jacop.constraints.Sum;
import org.jacop.constraints.XgtC;
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

        /**
         * Config stuff
         */
        int maxDiff = 1;

        // Input 1
        // int n = 9;
        // int[][] prefs = {{1,3}, {1,5}, {1,8},
        //     {2,5}, {2,9}, {3,4}, {3,5}, {4,1},
        //     {4,5}, {5,6}, {5,1}, {6,1}, {6,9},
        //     {7,3}, {7,8}, {8,9}, {8,7}};

        // Input 2
        // int n = 11;
        // int[][] prefs = {{1,3}, {1,5}, {2,5},
        //     {2,8}, {2,9}, {3,4}, {3,5}, {4,1},
        //     {4,5}, {4,6}, {5,1}, {6,1}, {6,9},
        //     {7,3}, {7,5}, {8,9}, {8,7}, {8,10},
        //     {9, 11}, {10, 11}};

        // Input 3
        int n = 15;
        int[][] prefs = {{1,3}, {1,5}, {2,5},
           {2,8}, {2,9}, {3,4}, {3,5}, {4,1},
           {4,15}, {4,13}, {5,1}, {6,10}, {6,9},
           {7,3}, {7,5}, {8,9}, {8,7}, {8,14},
           {9, 13}, {10, 11}};

        /**
         * End of config
         */

        Store store = new Store();

        // Create all variables
        IntVar[] vars = new IntVar[n];
        for(int i = 0; i < n; i++) {
            vars[i] = new IntVar(store, "var:"+Integer.toString(i), 1, n);
        }

        // Define constraints
        store.impose(new Alldiff(vars));

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

            IntVar dist = new IntVar(store, 0, n);
            Constraint d = new Distance(from, to, dist);
            store.impose(d);

            Constraint re = new Reified( new XgtC(dist, maxDiff), costs[i]);
            store.impose(re);
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
            System.out.println("Satisfied preferences: " + (prefs.length - cost.value()));

        } else {
            System.out.println("NOOOO! ");
        }
    }
}

