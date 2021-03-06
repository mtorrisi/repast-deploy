package infectionModel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.ContextUtils;

public class InfectionBuilder implements ContextBuilder<Object> {
	
	public static final String OUTPUT_FILENAME = "./output/output.txt";
		
	
	@Override
	public Context build(Context<Object> context) {
		context.setId("InfectionModel");

		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>(
				"infection network", context, true);
		netBuilder.buildNetwork();

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 50,
				50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 1500, 1500));

		Parameters params = RunEnvironment.getInstance().getParameters();
		int infectedCount = (Integer) params.getValue("infected_count");
		for (int i = 0; i < infectedCount; i++) {
			context.add(new Infected(space, grid));
		}

		int healthyCount = (Integer) params.getValue("healthy_count");
		for (int i = 0; i < healthyCount; i++) {
			context.add(new Healthy(space, grid));
		}
		
		int susceptibleCount = (Integer) params.getValue("susceptible_count");
		for (int i = 0; i < susceptibleCount; i++) {
			context.add(new Susceptible(space, grid));
		}
		
		

		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
		
	
		int endTime = ((Integer) params.getValue("end_time"))*365;
			RunEnvironment.getInstance().endAt(endTime);
			
			RunEnvironment.getInstance().getCurrentSchedule().schedule(ScheduleParameters.createOneTime(0), this, "createOut");
			RunEnvironment.getInstance().getCurrentSchedule().schedule(ScheduleParameters.createRepeating(0, 91.25, ScheduleParameters.LAST_PRIORITY), this, "writeOut");
			RunEnvironment.getInstance().getCurrentSchedule().schedule(ScheduleParameters.createRepeating(365, 365, ScheduleParameters.FIRST_PRIORITY), this, "outbreak", space, grid);
			
		return context;
	}
	
	public void outbreak(ContinuousSpace<Object> space, Grid<Object> grid){
		Context<Object> context = RunState.getInstance().getMasterContext();
		Parameters params = RunEnvironment.getInstance().getParameters();

		List<Object> susceptibles = new ArrayList<Object>();
		List<Object> healthy = new ArrayList<Object>();
		List<Object> infected = new ArrayList<Object>();
		
		for (Object obj : context) {
			if (obj instanceof Susceptible) {
				susceptibles.add(obj);
			}
			if (obj instanceof Healthy) {
				healthy.add(obj);
				
			}
			if (obj instanceof Infected) {
				infected.add(obj);
				
			}
			
		}
	/*	
		System.out.println(susceptibles.size() );
		System.out.println(healthy.size() );
		System.out.println(infected.size() );
	*/
		int susceptibleCount = (Integer) params.getValue("susceptible_count");
		int healthyCount = (Integer) params.getValue("healthy_count");
		int infectedCount = (Integer) params.getValue("infected_count");
	
		if ((susceptibles.size() - susceptibleCount) > 0) {
			
			for (int i = 0; i < (susceptibles.size() - susceptibleCount); i++) {
			Object obj = susceptibles.get(i);
			NdPoint spacePt = space.getLocation(obj);
			Context<Object> context1 = ContextUtils.getContext(obj);
			context.remove(obj);
		}
		}
		else if((susceptibles.size() - susceptibleCount) < 0){
		for (int i = 0; i < ( susceptibleCount - susceptibles.size()); i++) {
			context.add(new Susceptible(space, grid));
			
		}
		}
	
		if ((healthy.size() - healthyCount) > 0) {
			
			for (int i = 0; i < (healthy.size() - healthyCount); i++) {
			Object obj = healthy.get(i);
			NdPoint spacePt = space.getLocation(obj);
			Context<Object> context1 = ContextUtils.getContext(obj);
			context.remove(obj);
		}
		}
		else if((healthy.size() - healthyCount) < 0){
		for (int i = 0; i < ( healthyCount - healthy.size()); i++) {
			context.add(new Healthy(space, grid));
			
		}
		}
		
if ((infected.size() - infectedCount) > 0) {
			
			for (int i = 0; i < (infected.size() - infectedCount); i++) {
			Object obj = infected.get(i);
			NdPoint spacePt = space.getLocation(obj);
			Context<Object> context1 = ContextUtils.getContext(obj);
			context.remove(obj);
		}
		}
		else if((infected.size() - infectedCount) < 0){
		for (int i = 0; i < ( infectedCount - infected.size()); i++) {
			context.add(new Infected(space, grid));
			
		}
		}
		
		
		
		

		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
		
	
		
	}
	
	public void createOut(){
		//TODO
		
		try{
			  // Create file 
			String filename = OUTPUT_FILENAME;
			//String filename = "output.csv";
			  FileWriter fw = new FileWriter(filename);
			  BufferedWriter out = new BufferedWriter(fw);
			  out.write("Tick"+", "+"Healthy population"+", "+"Susceptible population"+", "+"Infected population\r\n");
			  //Close the output stream
			  out.close();
		}catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
		
	}

	public void writeOut(){
		
		Context<Object> context = RunState.getInstance().getMasterContext();
		
		List<Object> susceptibles = new ArrayList<Object>();
		List<Object> healthy = new ArrayList<Object>();
		List<Object> infected = new ArrayList<Object>();
		
		for (Object obj : context) {
			if (obj instanceof Susceptible) {
				susceptibles.add(obj);
			}
			if (obj instanceof Healthy) {
				healthy.add(obj);
				
			}
			if (obj instanceof Infected) {
				infected.add(obj);
				
			}
			
		}
	
		System.out.println(susceptibles.size() );
		System.out.println(healthy.size() );
		System.out.println(infected.size() );

		try{
			double currentTick = RepastEssentials.GetTickCount();
			String filename = OUTPUT_FILENAME;
			  FileWriter fw = new FileWriter(filename, true);
			  BufferedWriter out = new BufferedWriter(fw);
			  out.write(String.valueOf(currentTick)+", "
					  +String.valueOf(healthy.size())+", "
					  +String.valueOf(susceptibles.size())+", "
					  +String.valueOf(infected.size())+"\r\n");
			  
			  //Close the output stream
			  out.close();
		  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
	
		 }
		
	}
	

}
