# Flying Islands

## Description
This is a mod made using the Fabric API that adds Flying Islands to Minecraft. I made this because I play a lot of Skyblock challenges and always wanted my bases to be on natural looking Islands in the sky

## Features
- Flying Islands
- Fully Customizable Island Generation
- Customizable Island Sizes
- Customizable Island Heights
- Currently only has Plains Biome Islands
## Examples

![Double River Island](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/PersonalFavouriteSeed132.png?raw=true)

![Small Simple Island](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/SmallAndSimple.png?raw=true)

![Water Wrapped Island](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/InterestingOnes.png?raw=true)

## Goals
- Custom Islands for all Biomes and Nether Biomes
- Customizable Island Spawning in the Overworld, Nether and End with natural world generation



## Technical Description
This mod uses the Fabric API to add custom item to the game that can spawn flying islands.
All aspects of the islands are customized using seeded randomness.

The shape of the Island is spit into 3 parts:
- The Ground Layer
- The Underside
- The Topside

### Ground Layer

The ground layer is the base of the island. It is a 2D Array of blocks, which are circles that have 1D Polar OpenSimplex Noise applied to them. The values that determine how much noise is applied is the Ground Frequency and Ground Magnitude This creates a natural looking island shape. The ground layer is then filled in with blocks to allow for the rest of the generation.

### Underside
The underside is the bottom of the island. The distance of each blocks distance from the edge of the island is calculated and then a noise value using different custom is applied to it. This creates a natural looking underside to the island with variable depth and shape.

### Topside
For the Topside a 2D OpenSimplex Noise is applied to each block of the Ground Layer, then those noise values are added which creates small hills and valleys.

### Lake Generation
Lakes are made by taking the lowest area of noise in the topside and amplifying it downwards. This creates a natural looking lake or body of water on the island. The body is then filled using a 3D Floodfill algorithm to fill the lake with water.

### Decorations
Right now the island is just a shape. To make it appear more natural decorations are used, such as adding dirt and grass to the surface of the island, sand or clay replaces blocks touching water, and biome relevant plants are added to the island and water.