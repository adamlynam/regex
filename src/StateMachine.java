/*
 * StateMachine.java
 *
 * Created on 11 September 2005, 22:13
 */

/**
 *
 * @author Mad_Fool
 */
public class StateMachine
{
    public char[] character;
    public int[] next1;
    public int[] next2;
    
    /** Creates a new instance of StateMachine */
    public StateMachine()
    {
        character = new char[50];
        next1 = new int[50];
        next2 = new int[50];
    }
    
    public void setState(int index, char newCharacter, int newNext1, int newNext2)
    {
        character[index] = newCharacter;
        next1[index] = newNext1;
        next2[index] = newNext2;
    }
    
    public void print()
    {
        for(int i = 0; i < 20; i++)
        {
            System.out.println("Node " + i + " consumes " + character[i] + " -> " + next1[i] + " and " + next2[i]);
        }
    }
}
