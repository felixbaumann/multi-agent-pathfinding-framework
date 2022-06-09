/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package cooperative_a_star;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedEdge;
import multi_agent_pathfinding_framework.TimedPosition;

/* Reservation table for both, cooperative A* and the Token Passing algorithm.
 */
public class ReservationTable {

	/* All reservations.
	 * Edges, temporary positions, goal positions until the end of time. */
    private HashMap<Reservation, Integer> reservations
        = new HashMap<Reservation, Integer>();
    
    /* The same reservations but accessible by agent. */
    private HashMap<Agent, HashMap<Reservation, Integer>> agentsReservations
        = new HashMap<Agent, HashMap<Reservation, Integer>>();
    
    /* The positional reservations but accessible by position. */
    private HashMap<Position, HashSet<Integer>> positionalReservations
        = new HashMap<Position, HashSet<Integer>>();
    
    
    public ReservationTable(Agent[] agents) {
    	
    	for (Agent agent : agents) {

    		agentsReservations.put(agent,
    			new HashMap<Reservation, Integer>());
    	}
    }
   
    
    /* Check whether a given position is free at a given time. */
    public boolean isFree(TimedPosition timedPosition) {
    	
    	/* Is there a reservation for the position at this exact time? */
    	if (reservations.containsKey(new ReservationPosition(
    			timedPosition))) {
    		
    		return false;
    	}
    	
    	/* Is there a permanent reservation for the given position? */
    	if (reservations.containsKey(new ReservationFinalPosition(
    			timedPosition.position()))) {
    		
    		/* When does the permanent reservation start? */
    		int reservationTime = reservations.get(
    				new ReservationFinalPosition(timedPosition.position()));
    		
    		/* If the permanent reservation starts later than the requested
    		 * time, the position is free. */
    		return reservationTime > timedPosition.t;
    	}
    	
    	/* There's no reservation for the given time and no permanent
    	 * reservation that started earlier. */
    	return true;
    }
    
    
    /* Check whether a given position is free at a given time and whether
     * there's no reservation for it in the future.
     * Use this function before permanently reserving a position. */
    public boolean isFreeForever(TimedPosition timedPosition) {
    	
    	/* Checks for reservations in the past. */
    	if (!isFree(timedPosition)) return false;
    	
    	/* Checks for reservations in the future. */
    	if (!positionalReservations.containsKey(timedPosition.position())) {
    		
    		return true;
    	}
    	
    	for (int reservedTime : positionalReservations.get(
    			timedPosition.position())) {
    	
    		if (timedPosition.t < reservedTime) return false;
    	}
    	return true;
    }
    
    /* Assume some agent is at the given position at the given time.
     * Considering to stay there potentially forever, requires a check whether
     * the position is reserved at some time in the future.
     * This function returns true if this is not the case.
     * To avoid that the agent blocks itself, cancel its own plan first.
     */
    public boolean restingAllowed(Position position, int now) {
    	
    	if (positionalReservations.containsKey(position)) {
    	
    	    for (int time : positionalReservations.get(position)) {
    		
    		    if (time > now) return false;
    	    }
    	}
    	return true;
    }
    
    /* Check whether a given edge is free at a given time. 
     * This requires to also check whether a potential equivalent edge in the
     * opposite direction is reserved.*/
    public boolean isFree(TimedPosition start, TimedPosition target) {
    	
    	TimedEdge forwards = new TimedEdge(start, target);
    	
    	/* The backwards edge points in the opposite direction but still
    	 * at the same time. */
    	TimedEdge backwards = new TimedEdge(
    			new TimedPosition(target.position(), start.t),
    			new TimedPosition(start.position(), target.t));
    	
    	return !reservations.containsKey(new ReservationEdge(forwards)) &&
    		   !reservations.containsKey(new ReservationEdge(backwards));
    }
    
    
    /* Reserve a position at a given time for a given agent. Either just for
     * this time step or permanently. 
     * Call isFree() or isFreeForever() before, respectively. */
    public void reserve(Agent agent,
    		            TimedPosition timedPosition,
    		            boolean permanent) {
	
    	Reservation reservation = new ReservationPosition(timedPosition);
    	
    	/* All reservations. */
    	reservations.put(reservation, timedPosition.t);
		
    	/* Reservations ordered by their agent. */
		agentsReservations.get(agent).put(reservation, timedPosition.t);
		
    	/* Reservations ordered by their position. */
    	addPositionalReservation(timedPosition);
    	
    	if (permanent) {
    		
    		Reservation permanentReservation
    		    = new ReservationFinalPosition(timedPosition.position());
    		
    		/* All reservations. */
        	reservations.put(permanentReservation, timedPosition.t);
    		
        	/* Reservations ordered by their agent. */
    		agentsReservations.get(agent).put(permanentReservation,
    				                          timedPosition.t); 		
    	}
    }
    
    
    /* Reserve a given edge at a given time for a given agent. */
    public void reserve(Agent agent,
    		            TimedPosition start,
    		            TimedPosition target) {
    	
    	ReservationEdge reservation
    	    = new ReservationEdge(new TimedEdge(start, target));
 
    	reservations.put(reservation, start.t);
		
		agentsReservations.get(agent).put(reservation, start.t);

    }

    
    /* Cancel all reservations of the given agent. */
    public void cancelReservations(Agent agent) {    	
    	
    	/* Iterator allows to remove elements from the set it's
    	 * iterating over. */
    	for (Iterator<Reservation> iterator
    			= agentsReservations.get(agent).keySet().iterator();
    			iterator.hasNext();) {
    		
    		Reservation reservation = iterator.next();
    		
    		int reservationTime = agentsReservations.get(agent).get(reservation);

    		/* Remove it from the general reservation set. */
    		reservations.remove(reservation);
    			
    		/* Remove it from the reservation map accessible by position. */
    		removePositionalReservation(reservation, reservationTime);
    			
    		/* Remove it from the agent's individual reservation set. */
    		iterator.remove();
    	}    	
    	return;
    }

    
    /* Reservations for positions (be it temporary or forever) should also be
     * stored accessible by the position. This is done here.  */
    private void addPositionalReservation(TimedPosition timedPosition) {
    	
    	Position position = timedPosition.position();
    	
    	/* Initialize set if necessary. */
    	if (!positionalReservations.containsKey(position)) {
    		
    		positionalReservations.put(position, new HashSet<Integer>());
    	}
    	
    	positionalReservations.get(position).add(timedPosition.t);
    }
    
    
    /* If the given reservation is positional (be it temporary or permanent),
     * the reservation is removed from the positional reservation map. */
    private void removePositionalReservation(Reservation reservation, int time) {
    	
    	if (reservation instanceof ReservationPosition) {
    		
    		Position position = ((ReservationPosition) reservation).position();
    		
    		positionalReservations.get(position).remove(time);
    	}
    	
    	else if (reservation instanceof ReservationFinalPosition ) {
    		
    		Position position = ((ReservationFinalPosition) reservation).position();
    		
    		positionalReservations.get(position).remove(time);
    	}
    }
    
    
    /* Creates a string containing all reservations of the given time. */
    public String print(int time) {
    	
    	String string = "";
    	
    	for (Reservation reservation : reservations.keySet()) {
    		
    		if (reservations.get(reservation) == time) {
    			
    			string += reservation.print();
    		}    		
    	}    	
    	return string;
    }
}
