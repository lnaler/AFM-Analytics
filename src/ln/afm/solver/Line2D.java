/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * NOTICE: Changes in the file are limited to changing package location. Original
 * package location is org.ddogleg.example.
 */

package ln.afm.solver;

/**
 * Line in 2D space parameterized by the closest point to the origin.
 *
 * @author Peter Abeles
 */
public class Line2D {
	double x;
	double y;

	public String toString() {
		return "Line2D( x="+x+" y="+y+" )";
	}
}
