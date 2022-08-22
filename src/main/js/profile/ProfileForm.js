import React from 'react';
import Contact from "../common/Contact";
import Identifiers from "../common/Identifiers";
import Details from "../common/Details";

const ProfileForm = (props) => {
	
	const picture = props.bunny.picture
					? props.bunny.picture
					: '/assets/bunny.jpg';
			
	const bunnyGender = props.bunny.gender==='Unknown'
						?	'Okänt kön'
						:	(props.bunny.neutered?'Kastrerad ':'Ej kastrerad ') +
							(props.bunny.gender==='Female'?'hona':'hane');
	const bunnyBirthDate = props.bunny.birthDate
						?	', födelsedag '+props.bunny.birthDate
						:	'';
					
	const bunnyDetails = <div>
							<Identifiers bunny={props.bunny} />
							<hr/>
							<Details bunny={props.bunny} />
						</div>;
				
	return (
	<div className="row">
		<div className="col large justify-content-center">
			<div className="text-center">
				<a href={picture} rel="noopener noreferrer" target="_blank"><img className="bunny-picture-profile" src={picture}/></a>
			<div className="bunny-name mega-large">
				{props.bunny.name}
			</div>
			<div>{bunnyGender}{bunnyBirthDate}</div>
			</div>
			<hr/>
			<div className="mt-2">{bunnyDetails}</div>
		</div>
	</div>
	);
}

export default ProfileForm;