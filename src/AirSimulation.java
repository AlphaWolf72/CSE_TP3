
/* AirSimulation class
 *
 * TP of SE (version 2020)
 *
 * AM
 */

import java.util.Random;
import java.util.ArrayList;

public class AirSimulation
{
   private int nAgent1;
   private int nAgent2;
   private int nAgent3;
   private int nAgent4;
   private Aircraft a;
   public final int nagents = 4;

   // Constructor
   public AirSimulation()
   {
      this.nAgent1 = 0;
      this.nAgent2 = 0;
      this.nAgent3 = 0;
      this.nAgent4 = 0;
      this.a = new Aircraft();  // standard model
   }

   // Reference to Aircraft
   public Aircraft getAircraftRef()
   {
      return this.a;
   }

   // Agent1
   public void agent1() throws InterruptedException
   {
      boolean placed = false;
      Random R = new Random();
      ArrayList<Integer> emergRows = this.a.getEmergencyRowList();

      // generating a new Customer
      Customer c = new Customer();

      // randomly pick a seat
      do
      {
         int row = R.nextInt(this.a.getNumberOfRows());
         int col = R.nextInt(this.a.getSeatsPerRow());

         // verifying whether the seat is free
         if (this.a.isSeatEmpty(row,col))
         {
            // if this is an emergency exit seat, and c is over60, then we skip
            if (!emergRows.contains(row) || !c.isOver60() || this.a.numberOfFreeSeats() <= this.a.getSeatsPerRow() * this.a.getNumberEmergencyRows())
            {
               this.a.add(c,row,col);
               placed = true;
            }
         }
      }
      while (!placed && !this.a.isFlightFull());

      // updating counter
      if (placed)  this.nAgent1++;
   }

   // Agent2
   public void agent2() throws InterruptedException
   {
      boolean placed = false;
      ArrayList<Integer> emergRows = this.a.getEmergencyRowList();

      // generating a new Customer
      Customer c = new Customer();

      // searching free seats on the seatMap
      int row = 0;
      while (!placed && !this.a.isFlightFull() && row < this.a.getNumberOfRows())
      {
         int col = 0;
         while (!placed && col < this.a.getSeatsPerRow())
         {
            // verifying whether the seat is free
            if (this.a.isSeatEmpty(row,col))
            {
               // if this is an emergency exit seat, and c needs assistance, then we skip
               if (!emergRows.contains(row) || !c.needsAssistence() || this.a.numberOfFreeSeats() <= this.a.getSeatsPerRow() * this.a.getNumberEmergencyRows())
               {
                  this.a.add(c,row,col);
                  placed = true;
               }
            }
            col++;
         }
         row++;
      }

      // updating counter
      if (placed)  this.nAgent2++;
   }

   // Agent3
   public void agent3() throws InterruptedException
   {
      Random R = new Random();

      int row = 0;
      int col = 0;
      int row2 = 0;
      int col2 = 0;

      // randomly pick a seat
      while (this.a.isSeatEmpty(row,col) || this.a.isSeatEmpty(row2,col2))
      {
         row = R.nextInt(this.a.getNumberOfRows());
         col = R.nextInt(this.a.getSeatsPerRow());
         row2 = R.nextInt(this.a.getNumberOfRows());
         col2 = R.nextInt(this.a.getSeatsPerRow());
      }

      // generating a new Customer
      Customer c = this.a.getCustomer(row,col);
      Customer c2 = this.a.getCustomer(row2,col2);

      // checking frequent flyer numbers
      if (c.getFlyerLevel() > c2.getFlyerLevel())
      {
         this.a.freeSeat(row,col);
         this.a.freeSeat(row2,col2);
         this.a.add(c,row2,col2);
         this.a.add(c2,row,col);
      }
      // updating counter
      this.nAgent3++;
   }

   // Agent4: the virus
   public void agent4() throws InterruptedException
   {
      for (int i = 0; i < this.a.getNumberOfRows(); i++)
      {
         for (int j = 0; j < this.a.getSeatsPerRow(); j++)
         {
            Customer c = this.a.getCustomer(i,j);
            this.a.freeSeat(i,j);
            if (c != null) this.a.add(c,i,j);
         }
      }
      this.nAgent4++;
   }

   // Resetting
   public void reset()
   {
      this.nAgent1 = 0;
      this.nAgent2 = 0;
      this.nAgent3 = 0;
      this.nAgent4 = 0;
      this.a.reset();
   }

   // Printing
   public String toString()
   {
      String print = "AirSimulation (agent1 : " + this.nAgent1 + ", agent2 : " + this.nAgent2 + ", " +
              "agent3 : " + this.nAgent3 + ", agent4 : " + this.nAgent4 + ")\n";
      print = print + a.toString();
      return print;
   }

   class Agent extends Thread{

      private Thread t;
      private String nameAgent;
      private AirSimulation s;

      public Agent(String nameAgent, AirSimulation s){
         this.nameAgent = nameAgent;
         this.s = s;
      }

      @Override
      public void run(){
         try{
            switch(nameAgent){
               case "Agent2":
                  while (!s.getAircraftRef().isFlightFull()){
                     s.agent2();
                  }
                  break;
               case "Agent3":
                  while (!s.getAircraftRef().isFlightFull()){
                     s.agent3();
                  }
                  break;
               case "Agent4":
                  while (!s.getAircraftRef().isFlightFull()){
                     s.agent4();
                  }break;
            }
         }catch(InterruptedException e){
            e.printStackTrace();
         }
      }

      public void start(){
         if(t == null){
            t = new Thread(this, nameAgent);
            t.start();
         }
      }
   }

   // Simulation in parallel (main)
   public static void main(String[] args) throws InterruptedException
   {
      System.out.println("\n** Sequential execution **\n");
      long startSequentitalExec = System.currentTimeMillis();
      if (args != null && args.length > 0 && args[0] != null && args[0].equals("animation"))
      {
         AirSimulation s = new AirSimulation();
         while (!s.a.isFlightFull())
         {
            s.agent1();
            s.agent2();
            s.agent3();
            s.agent4();
            System.out.println(s + s.a.cleanString());
            Thread.sleep(100);
         }
         System.out.println(s);
      }
      else
      {
         AirSimulation s = new AirSimulation();
         while (!s.a.isFlightFull())
         {
            s.agent1();
            s.agent2();
            s.agent3();
            s.agent4();
         }
         System.out.println(s);
      }
      long endSequentialExec = System.currentTimeMillis();
      long sequentialExec = endSequentialExec - startSequentitalExec;
      System.out.println("Sequential execution time: " + sequentialExec);

      System.out.println("\n** Parallel execution **\n");
      long startParallelExec = System.currentTimeMillis();
      if (args != null && args.length > 0 && args[0] != null && args[0].equals("animation"))
      {
         AirSimulation s = new AirSimulation();
         Agent agent2 = s.new Agent("Agent2", s);
         Agent agent3 = s.new Agent("Agent3", s);
         Agent agent4 = s.new Agent("Agent4", s);
         agent2.start();
         agent3.start();
         agent4.start();
         while (!s.getAircraftRef().isFlightFull())
         {
            s.agent1();
            System.out.println(s + s.getAircraftRef().cleanString());
            Thread.sleep(100);
         }
         System.out.println(s);
      }
      else
      {
         AirSimulation s = new AirSimulation();
         Agent agent2 = s.new Agent("Agent2", s);
         Agent agent3 = s.new Agent("Agent3", s);
         Agent agent4 = s.new Agent("Agent4", s);

         agent2.start();
         agent3.start();
         agent4.start();
         while (!s.getAircraftRef().isFlightFull())
         {
            s.agent1();
         }
         System.out.println(s);
      }
      long endParallelExec = System.currentTimeMillis();
      long parallelExec = endParallelExec - startParallelExec;

      System.out.println("Parallel execution time: " + parallelExec);
   }
}

