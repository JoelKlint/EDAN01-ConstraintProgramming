import java.util.Arrays;

import org.jacop.constraints.Constraint;
import org.jacop.constraints.Element;
import org.jacop.constraints.LexOrder;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.SumInt;
import org.jacop.constraints.XplusYeqC;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleMatrixSelect;

public class Main {

    /**
     * Home = 1
     * Shop = 0
     */

	public static void main(String[] args) {

        /**
         * Input 1
         */
        // int n = 5;
        // // int n_commercial = 13;
        // int n_residential = 12;
        // int[] point_distribution = {-5, -4, -3, 3, 4, 5};

        /**
         * Input 2
         */
        // int n = 5;
        // // int n_commercial = 7;
        // int n_residential = 18;
        // int[] point_distribution = {-5, -4, -3, 3, 4, 5};

        /**
         * Input 3
         */
        int n = 7;
        // int n_commercial = 20;
        int n_residential = 29;
        int[] point_distribution = {-7, -6, -5, -4, 4, 5, 6, 7};

        /**
         * Create store
         */
        Store store = new Store();

        /**
         * Create the model
         * Residental = 1
         * Commercial = 0
         */
        IntVar[][] grid = new IntVar[n][n];
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                grid[i][j] = new IntVar(store, 0, 1);
            }
        }

        // Make sure there are correct number of residentials
        IntVar amountOfResidentialsInGrid = new IntVar(store, n_residential, n_residential);
        IntVar[] flattenedGrid = new IntVar[grid.length * grid[0].length];
        int index = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                flattenedGrid[index] = grid[i][j];
                index++;
            }
        }
        PrimitiveConstraint res_in_grid_constraint = new SumInt(store, flattenedGrid, "==", amountOfResidentialsInGrid);
        store.impose(res_in_grid_constraint);

        // Set variable holding residential sum for every row
        IntVar[] residentialsPerRow = new IntVar[n];
        for(int i = 0; i < n; i++) {
            IntVar row = new IntVar(store, 0, n);
            Constraint sumConstraint = new SumInt(store, grid[i], "==", row);
            store.impose(sumConstraint);
            residentialsPerRow[i] = row;
        }

        // Set variable holding residentials sum for every column
        IntVar[] residentialsPerCol = new IntVar[n];
        for(int i = 0; i < n; i++) {
            IntVar col = new IntVar(store, 0, n);
            Constraint sumConstraint = new SumInt(store, getColumn(grid, i), "==", col);
            store.impose(sumConstraint);
            residentialsPerCol[i] = col;
        }

        // Find least possible row/col score
        int minRowScore = Arrays.stream(point_distribution).min().getAsInt();
        int maxRowScore = Arrays.stream(point_distribution).max().getAsInt();

        // Set score variable for every row and col
        IntVar[] allScores = new IntVar[n * 2];
        for(int i = 0; i < n; i++) {
            // Row
            IntVar score = new IntVar(store, minRowScore, maxRowScore);
            store.impose(new Element(residentialsPerRow[i], point_distribution, score, -1));
            allScores[i] = score;

            // Col
            score = new IntVar(store, minRowScore, maxRowScore);
            store.impose(new Element(residentialsPerCol[i], point_distribution, score, -1));
            allScores[i + n] = score;
        }
    
        // Set variable storing global score
        IntVar globalScore = new IntVar(store, minRowScore * n * 2, maxRowScore * n * 2);
        store.impose(new SumInt(store, allScores, "==", globalScore));

        // Negate score, since search is minimizing
        IntVar negatedGlobalScore = new IntVar(store, minRowScore * n * 2, maxRowScore * n * 2);
        store.impose(new XplusYeqC(globalScore, negatedGlobalScore, 0));

        // Ignore permutations of the same solution
        for (int i = 1; i < n - 1; i++) {
            store.impose(new LexOrder(grid[i], grid[i + 1]));
            store.impose(new LexOrder(getColumn(grid, i), getColumn(grid, i + 1)));
        }
    
        // Start search
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<IntVar>(grid, null, new IndomainMin<IntVar>());
    
        boolean result = search.labeling(store, select, negatedGlobalScore);
    
        if (result) {
            System.out.println("Score: " + globalScore.value());
            System.out.println("_____Grid_____");
            for(IntVar[] row : grid) {
                for(IntVar var : row) {
                    System.out.print(var.value() + " ");
                }
                System.out.println();
            }
        } else {
            System.out.println("No solution found.");
        }

    }

    public static IntVar[] getColumn(IntVar[][] matrix, int index) {
        IntVar[] col = new IntVar[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            col[i] = matrix[i][index];
        }
        return col;
    }

}
