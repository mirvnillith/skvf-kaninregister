import React from 'react';

const ApprovalFailed = (_) => {

    return (
		<div>
			<h2 className="text-center dark"> Signeringen misslyckades </h2>
			
			Du har nu två alternativ:
			<p/>
			<a href="/approval"><h4>Försök igen</h4></a>
			Det kan ha varit ett tillfälligt fel.
			<p/>
			<a href="/bunnies"><h4>Fortsätt ändå</h4></a>
			Även utan signering kan du få se vad du eventuellt redan har i kaninregistret, men du kan inte lägga till eller ändra på något.
		</div>
	);
}
export default ApprovalFailed;