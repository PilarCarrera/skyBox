
public class Pentagon {
	
	public float[] vertices, textureCoords;
	public float alt, longLado;
	
	private float x1, x2, y1, y2, z1, z2;
	
	public Pentagon(float h, float sL) {	
		
		alt = h;
		longLado = sL;
		
		x1 = (float)Math.sin(2*Math.PI / 5) * longLado;
		x2 = (float)Math.sin(4*Math.PI / 5) * longLado;
		
		y1 = alt/2;
		y2 = -alt/2;
		
		z1 = (float)Math.cos(2*Math.PI / 5) * longLado;
		z2 = (float)Math.cos(1*Math.PI / 5) * longLado;
		
		Init();
		
	}
	
	protected void Init() {	
		
		float[] vertTemp = { 0, y1, 0,  	0, y1, longLado,     	x1, y1, z1,  	//N  triangle, cara de arriba
							 0, y1, 0,  	x1, y1, z1,   	x2, y1, -z2, 	//NE triangle, cara de arriba
							 0, y1, 0,  	x2, y1, -z2,  	-x2, y1, -z2,	//SE triangle, cara de arriba
							 0, y1, 0, 		-x2, y1, -z2,  	-x1, y1, z1, 	//SW triangle, cara de arriba
							 0, y1, 0, 		-x1, y1, z1,   	0, y1, longLado,		//NW triangle, cara de arriba
							 
							 x1, y2, z1,  	0, y2, longLado,     	0, y2, 0,	  	//N triangle,  cara de abajo
							 x2, y2, -z2, 	x1, y2, z1,   	0, y2, 0,	 	//NE triangle,  cara de abajo
							 -x2, y2, -z2,	x2, y2, -z2,  	0, y2, 0,		//SE triangle,  cara de abajo
							 -x1, y2, z1,	-x2, y2, -z2,  	0, y2, 0,	 	//SW triangle,  cara de abajo
							 0, y2, longLado,		-x1, y2, z1,   	0, y2, 0,		//NW triangle,  cara de abajo
							 
							 x1, y2, z1,	x1, y1, z1,   	0, y1, longLado,  		//NE face, triangulo abajo
							 0, y1, longLado,		0, y2, longLado,  	  	x1, y2, z1,		//NE face, triangulo arriba
							 
							 x2, y2, -z2,  	x2, y1, -z2,	x1, y1, z1,		//SE face, triangulo abajo
							 x1, y1, z1,	x1, y2, z1,		x2, y2, -z2,	//SE face, triangulo arriba
							 
							 -x2, y2, -z2, 	-x2, y1, -z2,	x2, y1, -z2,	//S  face, triangulo abajo
							 x2, y1, -z2,	x2, y2, -z2,	-x2, y2, -z2,	//S  face, triangulo arriba
							 
							 -x1, y2, z1,  -x1, y1, z1,		-x2, y1, -z2,	//SW face, triangulo abajo
							 -x2, y1, -z2,	-x2, y2, -z2,	-x1, y2, z1,	//SW face, triangulo arriba
							 
							 0, y2, longLado,   	0, y1, longLado,		-x1, y1, z1,	//NW face, triangulo abajo
							 -x1, y1, z1,	-x1, y2, z1,	0, y2, longLado		//NW face, triangulo arriba
							 };
		
		vertices = vertTemp;
		
		z1 = (z1+z2)/(1+z2);
		
		x2 = (x1+x2)/(x1+x1);
		
		float[] textTemp = { .5f, .5f,	.5f, 1f,    1f,  z1,  	//N  triangle, cara de arriba
							 .5f, .5f,  1f,  z1,   	x2, 0f, 	//NE triangle, cara de arriba
							 .5f, .5f,  x2, 0f,  	1f-x2, 0f,	//SE triangle, cara de arriba
							 .5f, .5f, 	1f-x2, 0f, 	0f,  z1, 	//SW triangle, cara de arriba
							 .5f, .5f, 	0f,  z1,   	.5f,  1f,	//NW triangle, cara de arriba
							 
							 1f,  z1,   .5f,  1f,   .5f, .5f,  	//N triangle,  cara de abajo
							 x2, 0f,    1f,  z1,   	.5f, .5f, 	//NE triangle, cara de abajo
							 1f-x2, 0f, x2, 0f,  	.5f, .5f,	//SE triangle, cara de abajo
							 .0f,  z1, 	1f-x2, 0f,  .5f, .5f, 	//SW triangle, cara de abajo
							 .5f,  1f, 	0f,  z1,   	.5f, .5f,	//NW triangle, cara de abajo
							 
							 0, 0,  	0, 1,   	1, 1,		//NE face, triangulo de abajo
							 1, 1,		1, 0,     	0, 0,  		//NE face, triangulo de arriba							 
							 0, 0,  	0, 1,   	1, 1,		//SE face, triangulo de abajo
							 1, 1,		1, 0,     	0, 0,		//SE face, triangulo de arriba							 
							 0, 0,  	0, 1,   	1, 1,		//S  face, triangulo de abajo
							 1, 1,		1, 0,     	0, 0,		//S  face, triangulo de arriba							 
							 0, 0,  	0, 1,   	1, 1,		//SW face, triangulo de abajo
							 1, 1,		1, 0,     	0, 0,		//SW face, triangulo de arriba						 
							 0, 0,  	0, 1,   	1, 1,		//NW face, triangulo de abajo
							 1, 1,		1, 0,     	0, 0,		//NW face, triangulo de arriba	
							
		};
		
		textureCoords = textTemp;
	}
	
	public float[] getVertices() {
		return vertices;
	}
	
	public float[] getTextureCoordinates() {
		return textureCoords;
	}
}