
/* AirSimulation class
 *
 * TP of SE (version 2020)
 *
 * AM
 */

import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class AirSimulation
{
   private int nAgent1;
   private int nAgent2;
   private int nAgent3;
   private int nAgent4;
   private final Aircraft a;
   private static final Semaphore sem = new Semaphore(1);

   private static Semaphore[][] sem_tab;

   // Constructor
   public AirSimulation()
   {
      this.nAgent1 = 0;
      this.nAgent2 = 0;
      this.nAgent3 = 0;
      this.nAgent4 = 0;
      this.a = new Aircraft();  // standard model
      sem_tab = new Semaphore[this.a.getNumberOfRows()][this.a.getSeatsPerRow()];
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

         sem_tab[row][col].acquire();
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
         sem_tab[row][col].release();
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
            sem_tab[row][col].acquire();
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
            sem_tab[row][col].release();
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

      int row = R.nextInt(this.a.getNumberOfRows());
      int col = R.nextInt(this.a.getSeatsPerRow());
      int row2 = R.nextInt(this.a.getNumberOfRows());
      int col2 = R.nextInt(this.a.getSeatsPerRow());

      sem_tab[row][col].acquire();

      int i = 0;

      // randomly pick a seat
      while (this.a.isSeatEmpty(row,col))
      {
         sem_tab[row][col].release();

         row = R.nextInt(this.a.getNumberOfRows());
         col = R.nextInt(this.a.getSeatsPerRow());

         sem_tab[row][col].acquire();
         System.out.println(i++);
      }

      sem_tab[row2][col2].acquire();
      // randomly pick a seat
      while (this.a.isSeatEmpty(row2,col2) || row == row2 && col == col2)
      {
         if (row != row2 || col != col2)
            sem_tab[row][col].release();
         row2 = R.nextInt(this.a.getNumberOfRows());
         col2 = R.nextInt(this.a.getSeatsPerRow());
         if (row != row2 || col != col2)
            sem_tab[row2][col2].acquire();


         System.out.println(i++);
      }

      // generating a new Customer
      Customer c = this.a.getCustomer(row,col);
      Customer c2 = this.a.getCustomer(row2,col2);
      // checking frequent flyer numbers
      if ((c.getFlyerLevel() > c2.getFlyerLevel() && row > row2) || (c.getFlyerLevel() < c2.getFlyerLevel() && row > row2))
      {
         this.a.freeSeat(row,col);
         this.a.freeSeat(row2,col2);
         this.a.add(c,row2,col2);
         this.a.add(c2,row,col);
      }
      // updating counter
      sem_tab[row][col].release();
      sem_tab[row2][col2].release();
      this.nAgent3++;
   }

   // Agent4: the virus
   public void agent4() throws InterruptedException
   {
      for (int i = 0; i < this.a.getNumberOfRows(); i++)
      {
         for (int j = 0; j < this.a.getSeatsPerRow(); j++)
         {
            //sem_tab[i][j].acquire();
            Customer c = this.a.getCustomer(i,j);
            this.a.freeSeat(i,j);
            if (c != null) this.a.add(c,i,j);
            //sem_tab[i][j].release();
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
      print = print + a;
      return print;
   }

   // Simulation in parallel (main)
   public static void main(String[] args) throws InterruptedException
   {
      System.out.println("\n** Sequential execution **\n");
      long startSequentitalExec = System.currentTimeMillis();
      AirSimulation s = new AirSimulation();
      for(int i = 0; i < s.a.getNumberOfRows(); i++){
         for(int j = 0; j < s.a.getSeatsPerRow() ; j++){
            sem_tab[i][j] = new Semaphore(1);
         }
      }
      while (!s.a.isFlightFull())
      {
         s.agent1();
         s.agent2();
         s.agent3();
         s.agent4();
         if (args != null && args.length > 0 && args[0] != null && args[0].equals("animation"))
         {
            System.out.println(s + s.a.cleanString());
            Thread.sleep(100);
         }
      }
      System.out.println(s);

      long endSequentialExec = System.currentTimeMillis();
      long sequentialExec = endSequentialExec - startSequentitalExec;
      s.reset();

      System.out.println("Sequential execution time: " + sequentialExec + "ms");

      System.out.println("\n** Parallel execution **\n");
      long startParallelExec = System.currentTimeMillis();
      Thread agent2 = new Thread(() -> {
         while (!s.a.isFlightFull()){
            try {
               s.agent2();
            } catch (InterruptedException e) {
               throw new RuntimeException(e);
            }
         }
      });

      Thread agent3 = new Thread(() -> {
         while (!s.a.isFlightFull()){
            try {
               s.agent3();
            } catch (InterruptedException e) {
               throw new RuntimeException(e);
            }
         }
      });

      Thread agent4 = new Thread(() -> {
         while (!s.a.isFlightFull()){
            try {
               s.agent4();
            } catch (InterruptedException e) {
               throw new RuntimeException(e);
            }
         }
      });

      agent2.start();
      agent3.start();
      agent4.start();

      while (!s.a.isFlightFull())
      {
         s.agent1();
         if (args != null && args.length > 0 && args[0] != null && args[0].equals("animation"))
         {
            System.out.println(s + s.a.cleanString());
            Thread.sleep(100);
         }
      }
      System.out.println(s);

      long endParallelExec = System.currentTimeMillis();
      long parallelExec = endParallelExec - startParallelExec;

      System.out.println("Parallel execution time: " + parallelExec + "ms");
   }
}

