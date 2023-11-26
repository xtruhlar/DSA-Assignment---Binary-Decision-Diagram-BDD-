# DSA Assignment 2 - Binary Decision Diagram BDD
Create a program that implements a data structure called Binary Decision Diagram (BDD), specifically designed for representing Boolean functions. The program should include three main functions: `BDD_create`, `BDD_create_with_best_order`, and `BDD_use`.

## Implemented Functions

### 1. `BDD *BDD_create(string bfunction, string order);`
- Creates a reduced Binary Decision Diagram to represent a given Boolean function.
- Takes a Boolean function as a string expression and the order of variables.
- Returns a pointer to the created BDD structure.

### 2. `BDD *BDD_create_with_best_order(string bfunkcia);`
- Finds the best order of variables for the given Boolean function by exploring various orders.
- Calls `BDD_create` multiple times with different variable orders.
- Returns a pointer to the smallest BDD found.

### 3. `char BDD_use(BDD *bdd, string input_values);`
- Uses the created BDD for a specific combination of input values to obtain the Boolean function result.
- Traverses the BDD tree from root to leaf based on the input values.
- Returns the result as '1' or '0', or a negative value in case of an error.

## Implementation Notes
- The `BDD_create` function includes BDD reduction during creation.
- The solution penalizes if BDD reduction occurs after the full creation.

## Testing
- Ensure 100% correctness through iterative calls to `BDD_use`.
- Test with randomly generated Boolean functions for both `BDD_create` and `BDD_create_with_best_order`.
- Evaluate the BDD reduction rate and execution time for various numbers of variables.

## Documentation
- Include a detailed documentation describing the solution, individual functions, structures, testing methodology, and results.
- Provide a header with author information and assignment details.
- Explain the correctness of the solution, testing strategies, and efficiency evaluations.
- Estimate time and memory complexity for both `BDD_create` and `BDD_create_with_best_order`.
