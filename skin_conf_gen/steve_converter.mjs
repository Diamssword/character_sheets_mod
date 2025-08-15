import * as fs from "fs"
import sharp from "sharp";
import {Image}  from 'image-js';
if(!fs.existsSync("steves"))
fs.mkdirSync("steves")
readFolder("./steves").then(()=>console.log("Done!"))
async function readFolder(path)
{
    await fs.readdirSync(path).forEach(async element => {
        if(!fs.statSync(path+"/"+element).isDirectory())
           await convert(path+"/"+element)
        else
            readFolder(path+"/"+element);
            
    });
}
async function convert(path)
{
    console.log("Converting :"+path);

   
    
    let image = await Image.load(Buffer.from( fs.readFileSync(path,{encoding:"binary"}),"binary"));
    
    
    var b1=image.extract(new Image(8,24),{position:[105,40]});
    var b2=image.extract(new Image(8,24),{position:[105,40]});
    image = await Image.load(path);
    image.extract(new Image(17,24),{position:[95,40]})
    image.insert(b1,95,40).insert(b2,103,40).save(path,{format:"png"});
  //  var im=sharp(path);
   // im.extract({left:95,top:40,width:17,height:24});
    //var bf1=await sharp(path).extend({top:0,left:95,top:40,width:1,height:24,}).toBuffer()
    //let bu=await sharp(path).composite([{input:{create:{width:17,height:24,channels:4,background: { r: 0, g: 0, b: 0, alpha: 0 }}},left:95,top:40,blend: "clear" }]).toFile(path+"1");;
    //await im.composite([{input: b1,left:95,top:40},{input: b2,left:103,top:40}]).toFile(path+"1");
    
   
   // await image.composite([{input:temp,left:95,top:40}]).toFile(path)
    //await image.write(path);
}