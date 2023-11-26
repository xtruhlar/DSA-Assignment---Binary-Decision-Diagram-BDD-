public class BDDNode {
    /* Štruktúra BDDNode */
    private final String variable;
    private BDDNode low;
    private BDDNode high;
    private BDDNode parent;
    private String controlVariable;

    public BDDNode(String variable, BDDNode low, BDDNode high){
        this.variable = variable;
        this.low = low;
        this.high = high;
    }

    public String getVariable(){

        return this.variable;
    }

    public void setLow(BDDNode low){
        this.low = low;
    }

    public BDDNode getLow(){
        return this.low;
    }

    public void setHigh(BDDNode high){
        this.high = high;
    }

    public BDDNode getHigh(){
        return this.high;
    }

    public BDDNode getParent() {
        return parent;
    }

    public void setParent(BDDNode parent) {
        this.parent = parent;
    }

    public String getControlVariable(){
        return this.controlVariable;
    }

    public void setControlVariable(String controlVariable){
        this.controlVariable = controlVariable;
    }
}