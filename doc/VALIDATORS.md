# Current Validators
## AnyNamespacePresentValidator
Checks if the resource pack has a namespace folder (e.g. assets/**minecraft**). Although it's not required to have a namespace folder, it's most likely what you want.

## ModelIsJsonObjectValidator
Checks if all json files located in a namespaces models directory are JsonObjects. This is a basic check, which should never fail, but it's a requirement for all other model based checks.

## ModelParentValidator
Checks if a model has a valid parent, the parent needs to be inside the same resource pack or referencing a vanilla model. This check does not fail if no parent is set.

## ModelHasAnyTextureValidator
Checks if a model has at least one texture assigned to it. This check does not fail if the model has an existing parent model.

## ModelTexturesExistsValidator
Checks if the texture files referenced in the model exists.

## ModelTextureReferencesResolvableValidator
Checks if all hashprefixed referenced textures (e.g. #side) are bound to a texture. This validation is skipped if the file is used as a parent in any other model to allow template models.

## ModelOverridesExistsValidator
Checks if item overrides are correct and the referenced model exists.

## UnusedFileValidator
Checks every file if it has been referenced in any json file. Most files need to be referenced somewhere, but this check may falsely detect files, which are in use. This will not flag for vanilla texture/model overrides. Files can be ignored with the `ignore` option using shell globs or regex.

## TextureLimitMipLevelValidator
Checks every PNG file, that will be loaded into the texture atlas, if it limits the mipmap levels. If the mip level drops below 4, the game displays certain textures with lower quality (e.g. leaves).

## TextureIsNotCorruptedValidator
Checks every PNG file, that is located in any resource pack directory, if the texture is an actual image.

## FontTextureExistsValidator
Checks if the texture file referenced in the font provider exists.

## FontCharacterUsageValidator
Checks if a font file defines a character multiple times, which overrides the character.

## ModelRequiresOverlayOverrideValidator
Checks if a model json, which is present in the default pack, is also present in a specified overlay. This Validator requires some additional configuration to run and may be used to make sure every model in a specified path is overridden in a specific overlay (e.g. to fix certain models in certain game versions).