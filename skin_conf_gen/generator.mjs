import * as fs from "fs"

var tot={}
const base_chars = [
    '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
    'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
    'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    '-', '_', ' ','.','/'
  ];
const fix = str => str.normalize('NFKD').split('')
.map(c => base_chars.find(bc => bc.localeCompare(c, 'en', { sensitivity: 'base' })==0))
.join('');
fs.readdirSync("./input").forEach(element => {
    if(fs.statSync("./input/"+element).isDirectory())
        checkFolder(element,"./input",element,"")
        
});
const res=[];
for(var k in tot)
{
    res.push(tot[k])
}
fs.writeFileSync("cloths.json",JSON.stringify(res,null,3))
function checkFolder(folder,parent,type,id)
{
    fs.readdirSync(parent+"/"+folder).forEach(v=>{
        if(fs.statSync(parent+"/"+folder+"/"+v).isDirectory())
            checkFolder(v,parent+"/"+folder,type,id+v+"/")
        else
        {
            if(v.endsWith(".png"))
            {
                const id1=id+v.replace(".png","")
                if(!tot[flaten(id1)])
                {
                    tot[flaten(id1)]={id:flaten(id1), layer:type,name:toName(id1)}
                    const p="./textures/"+type;
                    //if(!fs.existsSync(p))
                    //  fs.mkdirSync(p)
                // fs.copyFileSync(parent+"/"+folder+"/"+v,p+"/"+flaten(v))
                }
                else 
                    console.log("Duplicate id: "+v)
            }
        }
    })
}


/**
 * 
 * @param {string} s 
 */
function flaten(s)
{
    return fix(s.replaceAll(" ","_").toLowerCase());
}
/**
 * 
 * @param {string} s 
 */
function toName(s)
{
        return titleCase(s.replaceAll("_"," ").replaceAll("-"," "));
 
}
function titleCase(str) {
    return str
      .toLowerCase()
      .split(' ')
      .map((word) => (word[0]||"").toUpperCase() + word.slice(1))
      .join(' ')
  }