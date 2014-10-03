/*
 * Regex.java
 *
 * Created on 11 September 2005, 14:19
 */

/**
 *
 * @author Mad_Fool
 */
public class Regex
{
    
    private String potentialExpression;
    private int index;
    private StateMachine machine;
    private int stateCount;
    
    private int initalNodeLatest;
    
    /** Creates a new instance of Regex */
    public Regex(String newExpression)
    {
        potentialExpression = newExpression;
        index = 0;
        machine = new StateMachine();
        stateCount = 1;
        
        initalNodeLatest = 0;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Regex testRegex = new Regex("((a)|(b))*[ghj](.)|(y)");
        if(testRegex.parseString())
        {
            System.out.println("This is a valid regular expression");
        }
        else
        {
            System.out.println("This is an invalid regular expression");
        }
        
        testRegex.machine.print();
    }
    
    public boolean parseString()
    {
        boolean initalExpression = true;
        while(index < potentialExpression.length())
        {
            if(!expression())
            {
                return false;
            }
            if(initalExpression)
            {
                machine.setState(0, ' ', initalNodeLatest, initalNodeLatest);           
                initalExpression = false;
            }
        }
        machine.setState(stateCount, ' ', -1, -1);
        return true;
    }

    private boolean expression()
    {
        if (index >= potentialExpression.length())
        {
            return false;
        }
        else if (potentialExpression.charAt(index) == '[')
        {
            if(!disjunctive())
            {
                return false;
            }
        }
        else if (potentialExpression.charAt(index) == '\\')
        {
            if(!escape())
            {
                return false;
            }
        }
        else if (potentialExpression.charAt(index) == '.')
        {
            if(!wildcard())
            {
                return false;
            }
        }
        else if (potentialExpression.charAt(index) == '(')
        {
            if(!bracket())
            {
                return false;
            }
        }
        else if (potentialExpression.charAt(index) == ']' || potentialExpression.charAt(index) == ')' || potentialExpression.charAt(index) == '|' || potentialExpression.charAt(index) == '*')
        {
           return false;
        }
        else
        {
            //must be a non-special character
            literal();
        }
        
        return true;
    }

    private boolean disjunctive()
    {
        initalNodeLatest = stateCount;
        machine.setState(stateCount, ' ', stateCount + 2, stateCount + 2);
        int lastBranch = stateCount;
        stateCount++;
        int endState = stateCount;
        stateCount++;
        index++;
        while (index < potentialExpression.length())
        {
            if (potentialExpression.charAt(index) == ']')
            {
                machine.setState(endState, ' ', stateCount, stateCount);
                machine.setState(stateCount, ' ', stateCount + 1, stateCount + 1);
                stateCount++;
                index++;
                return true;	
            }
            machine.next2[lastBranch] = stateCount;
            machine.setState(stateCount, ' ', stateCount + 1, stateCount + 1);
            lastBranch = stateCount;
            stateCount++;
            machine.setState(stateCount, potentialExpression.charAt(index), endState, endState);
            stateCount++;
            index++;
        }
        return false;
    }

    private boolean escape()
    {
        index++;
        if (index < potentialExpression.length())
        {
            machine.setState(stateCount, potentialExpression.charAt(index), stateCount + 1, stateCount + 1);
            initalNodeLatest = stateCount;
            stateCount++;
            index++;
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean wildcard()
    {
        machine.setState(stateCount, '.', stateCount + 1, stateCount + 1);
        initalNodeLatest = stateCount;
        stateCount++;
        index++;
        return true;
    }

    private boolean bracket()
    {
        int previousNode = stateCount - 1;
        index++;
        if(!expression())
        {
            return false;
        }
        if(potentialExpression.charAt(index) != ')')
        {
            return false;
        }
        index++;
        if(index < potentialExpression.length() && potentialExpression.charAt(index) == '|')
        {
            if(!or(previousNode))
            {
                return false;
            }
        }
        else if(index < potentialExpression.length() && potentialExpression.charAt(index) == '*')
        {
            if(!closure(previousNode, initalNodeLatest))
            {
                return false;
            }
        }
        return true;
    }

    private boolean or(int previousNode)
    {
        //because the initalNodeLatest variable will later be replaced with the second part of the OR
        int initalNodeFirst = initalNodeLatest;
        int endNodeFirst = stateCount - 1;
        index++;
        if(potentialExpression.charAt(index) != '(')
        {
            return false;
        }
        index++;
        if(!expression())
        {
            return false;
        }
        if(potentialExpression.charAt(index) != ')')
        {
            return false;
        }
        int endNodeLatest = stateCount - 1;
        machine.setState(stateCount, ' ', initalNodeFirst, initalNodeLatest);
        machine.next1[previousNode] = stateCount;
        machine.next2[previousNode] = stateCount;
        initalNodeLatest = stateCount;
        stateCount++;
        machine.setState(stateCount, ' ', stateCount + 1, stateCount + 1);
        machine.next1[endNodeFirst] = stateCount;
        machine.next2[endNodeFirst] = stateCount;
        machine.next1[endNodeLatest] = stateCount;
        machine.next2[endNodeLatest] = stateCount;
        stateCount++;
        index++;
        return true;
    }

    private boolean closure(int previousNode, int initalNode)
    {
        machine.setState(stateCount, ' ', stateCount + 1, initalNode);
        machine.next1[previousNode] = stateCount;
        initalNodeLatest = stateCount;
        stateCount++;
        index++;
        return true;
    }
    
    private boolean literal()
    {
        machine.setState(stateCount, potentialExpression.charAt(index), stateCount + 1, stateCount + 1);
        initalNodeLatest = stateCount;
        stateCount++;
        index++;
        if (index < potentialExpression.length() && potentialExpression.charAt(index) == '*')
        {
            if(!closure(stateCount - 2, stateCount - 1))
            {
                return false;
            }
        }
        //this is in case the concatenation code below executes and changes the initalNodeLatest value
        int overallInitalNode = initalNodeLatest;
        
        if(index < potentialExpression.length())
        {
            int indexStore = index;
            while(expression())
            {
                indexStore = index;
            }
            index = indexStore;
        }
        
        initalNodeLatest = overallInitalNode;
        
        return true;
    }
}
