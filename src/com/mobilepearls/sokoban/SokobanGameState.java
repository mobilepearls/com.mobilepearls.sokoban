package com.mobilepearls.sokoban;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class SokobanGameState implements Serializable {

	static class Undo implements Serializable {

		public char c1;
		public char c2;
		public char c3;

		public byte x1;
		public byte x2;
		public byte x3;

		public byte y1;
		public byte y2;
		public byte y3;
	}

	public static final char CHAR_DIAMOND_ON_FLOOR = '$';
	public static final char CHAR_DIAMOND_ON_TARGET = '*';
	public static final char CHAR_FLOOR = ' ';
	public static final char CHAR_MAN_ON_FLOOR = '@';
	public static final char CHAR_MAN_ON_TARGET = '+';
	public static final char CHAR_TARGET = '.';
	public static final char CHAR_WALL = '#';

	private static char newCharWhenDiamondPushed(char current) {
		return (current == CHAR_FLOOR) ? CHAR_DIAMOND_ON_FLOOR : CHAR_DIAMOND_ON_TARGET;
	}

	private static char newCharWhenManEnters(char current) {
		switch (current) {
		case CHAR_FLOOR:
		case CHAR_DIAMOND_ON_FLOOR:
			return CHAR_MAN_ON_FLOOR;
		case CHAR_TARGET:
		case CHAR_DIAMOND_ON_TARGET:
			return CHAR_MAN_ON_TARGET;
		}
		throw new RuntimeException("Invalid current char: '" + current + "'");
	}

	private static char originalCharWhenManLeaves(char current) {
		return (current == CHAR_MAN_ON_FLOOR) ? CHAR_FLOOR : CHAR_TARGET;
	}

	private static char[][] stringArrayToCharMatrix(String[] s) {
		char[][] result = new char[s[0].length()][s.length];
		for (int x = 0; x < s[0].length(); x++) {
			for (int y = 0; y < s.length; y++) {
				result[x][y] = s[y].charAt(x);
			}
		}
		return result;
	}

	private int currentLevel;
	final int currentLevelSet;
	private char[][] map;
	private transient final int[] playerPosition = new int[2];
	final LinkedList<Undo> undos = new LinkedList<Undo>();

	public SokobanGameState(int level, int levelSet) {
		currentLevel = level;
		currentLevelSet = levelSet;
		loadLevel(currentLevel, levelSet);
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public int getHeightInTiles() {
		return map[0].length;
	}

	public char getItemAt(int x, int y) {
		return map[x][y];
	}

	public int[] getPlayerPosition() {
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[0].length; y++) {
				char c = map[x][y];
				if (CHAR_MAN_ON_FLOOR == c || CHAR_MAN_ON_TARGET == c) {
					playerPosition[0] = x;
					playerPosition[1] = y;
				}
			}
		}
		return playerPosition;
	}

	public int getWidthInTiles() {
		return map.length;
	}

	public boolean isDone() {
		for (int x = 0; x < map.length; x++)
			for (int y = 0; y < map[0].length; y++)
				if (map[x][y] == CHAR_DIAMOND_ON_FLOOR)
					return false;
		return true;
	}

	private void loadLevel(int level, int levelSet) {
		this.currentLevel = level;
		map = stringArrayToCharMatrix(SokobanLevels.levelMaps.get(levelSet)[level]);
	}

	public boolean performUndo() {
		if (undos.isEmpty())
			return false;
		Undo undo = undos.removeLast();
		map[undo.x1][undo.y1] = undo.c1;
		map[undo.x2][undo.y2] = undo.c2;
		if (undo.c3 != 0)
			map[undo.x3][undo.y3] = undo.c3;
		return true;
	}

	public void restart() {
		loadLevel(currentLevel, currentLevelSet);
		undos.clear();
	}

	/** Return whether something was changed. */
	public boolean tryMove(int dx, int dy) {
		if (dx == 0 && dy == 0)
			return false;

		if (dx != 0 && dy != 0) {
			throw new IllegalArgumentException("Can only move straight lines. dx=" + dx + ", dy=" + dy);
		}

		int steps = Math.max(Math.abs(dx), Math.abs(dy));
		int stepX = (dx == 0) ? 0 : (int) Math.signum(dx);
		int stepY = (dy == 0) ? 0 : (int) Math.signum(dy);

		boolean somethingChanged = false;

		int playerX = -1;
		int playerY = -1;
		// find player position
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[0].length; y++) {
				char c = map[x][y];
				if (CHAR_MAN_ON_FLOOR == c || CHAR_MAN_ON_TARGET == c) {
					playerX = x;
					playerY = y;
				}
			}
		}

		for (int i = 0; i < steps; i++) {
			int newX = playerX + stepX;
			int newY = playerY + stepY;

			boolean ok = false;
			boolean pushed = false;

			switch (map[newX][newY]) {
			case CHAR_FLOOR:
				// move to empty space
			case CHAR_TARGET:
				// move to empty target
				ok = true;
				break;
			case CHAR_DIAMOND_ON_FLOOR:
				// pushing away diamond on floor
			case CHAR_DIAMOND_ON_TARGET:
				// pushing away diamond on target
				char pushTo = map[newX + stepX][newY + stepY];
				ok = (pushTo == CHAR_FLOOR || pushTo == CHAR_TARGET);
				// ok if pushing to empty space
				if (ok) {
					pushed = true;
				}
				break;
			}

			if (ok) {
				Undo undo;
				if (undos.size() > 2000) {
					// size of undo: 9 bytes + object overhead = 25?
					// reuse and clear undo object
					undo = undos.removeFirst();
					undo.c3 = 0;
				} else {
					undo = new Undo();
				}
				undos.add(undo);
				somethingChanged = true;

				if (pushed) {
					byte pushedX = (byte) (newX + stepX);
					byte pushedY = (byte) (newY + stepY);
					undo.x3 = pushedX;
					undo.y3 = pushedY;
					undo.c3 = map[pushedX][pushedY];
					map[pushedX][pushedY] = newCharWhenDiamondPushed(map[pushedX][pushedY]);
				}

				undo.x1 = (byte) playerX;
				undo.y1 = (byte) playerY;
				undo.c1 = map[playerX][playerY];
				map[playerX][playerY] = originalCharWhenManLeaves(map[playerX][playerY]);
				undo.x2 = (byte) newX;
				undo.y2 = (byte) newY;
				undo.c2 = map[newX][newY];
				map[newX][newY] = newCharWhenManEnters(map[newX][newY]);

				playerX = newX;
				playerY = newY;
				if (isDone()) {
					// if moving multiple steps at once, stop if an intermediate step may finish the game:
					return true;
				}
			}
		}
		return somethingChanged;
	}

	/**
	 * Find the shortest path from the player to the destination, if one exists.
	 * This method will refuse to push diamonds unless the player taps a diamond.
	 * It will only push a diamond when it is tapped if the player is directly adjacent.
	 * If the space tapped is empty, it simply tries to find a way to that point.
	 * If there is a path, it teleports to the destination and returns true.  If not, it returns false.
	 * @param dx
	 * @param dy
	 * @return
	 */
	public boolean tryTeleport(int dx, int dy)
	{
		//Before beginning the search, make sure the destination is valid.
		if( !(dx > -1 && dx < map.length && dy > -1 && dy < map[0].length) )
			return false;

		int playerX = -1;
		int playerY = -1;
		// find player position
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[0].length; y++) {
				char c = map[x][y];
				if (CHAR_MAN_ON_FLOOR == c || CHAR_MAN_ON_TARGET == c) {
					playerX = x;
					playerY = y;
				}
			}
		}
		
		if( map[dx][dy] != CHAR_FLOOR && map[dx][dy] != CHAR_TARGET )	//If it's not an empty spot...
		{
			//If it's a diamond, push it IF you're directly adjacent.
			if( map[dx][dy] == CHAR_DIAMOND_ON_FLOOR || map[dx][dy] == CHAR_DIAMOND_ON_TARGET )
			{
				if( (playerX == dx-1 && playerY == dy) || 
					(playerX == dx+1 && playerY == dy) ||
					(playerX == dx && playerY == dy-1) ||
					(playerX == dx && playerY == dy+1) )
				{
					return tryMove(dx-playerX, dy-playerY);//Make it an offset from the player
				}
			}
			//If it's not empty and not a diamond, we can't go there.
			return false;
		}
		
		ArrayList<Integer[]> elements = new ArrayList<Integer[]>();

		//Start queue at player.  If in the future there's a need to actually find the shortest path,
		//A third element will be needed to determine distance.
		elements.add(new Integer[]{playerX, playerY});//Starting at distance 0

		//elements will continue to expand,
		//but eventually it will fill all spaces and stop if there's no solution
		for(int i = 0; i < elements.size(); i++)
		{
			Integer[] loc = elements.get(i);

			//Note that ALL the levels are framed with walls, so there should
			//be no need to do bounds checks; it should be impossible to
			//get an out-of-bounds exception when performing this search
			//as long as there isn't a hole into the abyss
			for( int j = 0; j < 4; j++ )
			{
				Integer[] adjLoc = new Integer[2];

				switch( j )
				{
					case 0:						//Left side
						adjLoc[0] = loc[0]-1;
						adjLoc[1] = loc[1];
						//adjLoc[2] = loc[2]+1;
						break;
					case 1:						//Right side
						adjLoc[0] = loc[0]+1;
						adjLoc[1] = loc[1];
						//adjLoc[2] = loc[2]+1;
						break;
					case 2:						//Top side
						adjLoc[0] = loc[0];
						adjLoc[1] = loc[1]+1;
						//adjLoc[2] = loc[2]+1;
						break;
					case 3:						//Bottom side
						adjLoc[0] = loc[0];
						adjLoc[1] = loc[1]-1;
						//adjLoc[2] = loc[2]+1;
						break;
				}

				//We must determine that it is an empty spot and that we haven't already visited it.
				if(  (map[adjLoc[0]][adjLoc[1]] == CHAR_FLOOR || map[adjLoc[0]][adjLoc[1]] == CHAR_TARGET) &&
						!containsCoords(elements, adjLoc[0], adjLoc[1] ) )//Would have to do more comparisons here to actually show shortest path
				{
					if( adjLoc[0] == dx && adjLoc[1] == dy )//If this is equal to the destination, then we KNOW we can teleport to it.
					{
						//Perform teleport
						Undo undo;
						if (undos.size() > 2000) {
							// size of undo: 9 bytes + object overhead = 25?
							// reuse and clear undo object
							undo = undos.removeFirst();
							undo.c3 = 0;
						} else {
							undo = new Undo();
						}
						undos.add(undo);
						
						undo.x1 = (byte) playerX;
						undo.y1 = (byte) playerY;
						undo.c1 = map[playerX][playerY];
						map[playerX][playerY] = originalCharWhenManLeaves(map[playerX][playerY]);
						undo.x2 = (byte) dx;
						undo.y2 = (byte) dy;
						undo.c2 = map[dx][dy];
						map[dx][dy] = newCharWhenManEnters(map[dx][dy]);
						return true;
					}
					elements.add(adjLoc);
				}
			}
		}
		return false;//Path to destination not found
	}
	
	private boolean containsCoords(ArrayList<Integer[]> elements, int x, int y)
	{
		for(int i = 0; i < elements.size(); i++)
		{
			Integer[] element = elements.get(i);
			if( element[0] == x && element[1] == y )
				return true;
		}
		return false;
	}
}
