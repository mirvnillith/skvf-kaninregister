import React from 'react';

const ApprovalFailed = (_) => {

    return (
		<div>
			<h2 className="text-center dark"> Signeringen misslyckades </h2>
			
			Du har nu två alternativ:
			<p/>
			<a href="/approval"><b>Försök igen</b></a>
			<br/> Det kan ha varit ett tillfälligt fel.
			<p/>
			<a href="/bunnies"><b>Fortsätt ändå</b></a>
			<br/> Även utan signering kan du få se vad du eventuellt redan har i kaninregistret, men du kan inte lägga till eller ändra på något.
		</div>
	);
}
export default ApprovalFailed;