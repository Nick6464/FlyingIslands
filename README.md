# Flying Islands

## Description

This is a mod made using the Fabric API that adds Flying Islands to Minecraft. I made this because I play a lot of Skyblock challenges and always wanted my bases to be on natural looking Islands in the sky

## Features

- Flying Islands
- Fully Customizable Island Generation of any Minecraft Biome
- Customizable Island Sizes
- Customizable Island Heights

## Examples

![Double River Island](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/PersonalFavouriteSeed132.png?raw=true)

![Jungle Biome](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/FullyRandomBiomesJng.png?raw=true)

![Sakura](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/FullyRandomBiomesCherry2.png?raw=true)

![Savanna](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/FullyRandomBiomesSavahna.png?raw=true)

![Woods](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/FullyRandomIslands.png?raw=true)

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

Right now the island is just a shape. The Decorations are my attempt to spawn relevant features such as trees and flowers in a similar number to the overworld. I'm wanting to favour rarer features, since the space and land mass of islands is far lower than the standard over world

## Progess Images

Below is my first iteration of adding noise to the underside of the island to give it a natural "Pulled from the Earth" apperance
![Noisy Underside](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/RadialNoiseCircleWithExponentialUndersideNoise.png?raw=true)

This image show the new addition of a variable to control the shaping of the island so they don't appear as basic circles
![New Shaping Algorithm](https://github.com/Nick6464/FlyingIslands/blob/main/Progress%20Log/NewShapes.png?raw=true)
