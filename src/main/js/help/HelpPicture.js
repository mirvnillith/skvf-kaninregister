import React from 'react';

const HelpPicture = (_) => {

    return (
		<div>
			<h2 className="text-center dark"> Länk till bild </h2>
		
			Kaninregistret lagrar inga bilder och låter dig istället ange en länk till en bild på din kanin från någon annanstans.
			Antagligen har du redan bilder publicerade som du då enkelt kan återanvända här.
			Så hur gör du då för att få tag i en bildlänk?
			<p/>
			<h4>Publicerad bild</h4>
			Det enklaste sättet att få en länk till en bild du redan publicerat är att högerklicka på den. De allra flesta browsers har där då ett alternativ för
			att kopiera dess länk.
			<p/>
			<h4>Bild på datorn</h4>
			Givetvis kan inte Kaninregistret hämta bilder från din dator, men det finns ett antal gratistjänster på nätet där du enkelt kan lägga upp dem
			för att få en länk att använda här (tänk på att det är länken till bilden du vill ha och inte länken till sidan med bilden på):
			<p/>
			<ul>
			<li><a href="https://sv.imgbb.com/" rel="noopener noreferrer" target="_blank">ImgBB</a></li>
			<li><a href="https://postimages.org/" rel="noopener noreferrer" target="_blank">Postimages</a></li>
			<li><a href="https://freeimage.host/" rel="noopener noreferrer" target="_blank">Freeimage</a></li>
			<li><a href="https://imgur.com/upload" rel="noopener noreferrer" target="_blank">imgur</a></li>
			</ul>
		</div>
	)
}

export default HelpPicture;