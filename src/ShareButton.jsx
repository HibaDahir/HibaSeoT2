import './ShareStyle.css';

function ShareButton({ kurs }) {
    // Åpner delemenyen i nettleseren/mobilen. Støttes ikke det, kopieres teksten til utklippstavlen
    function del() {
        const dato = new Date(kurs.dato).toLocaleDateString('nb-NO');
        const data = {
            title: kurs.tittel,
            text: `${kurs.tittel} - ${dato} (${kurs.lokallag})`,
            url: window.location.href,
        };
        if (navigator.share) {
            navigator.share(data).catch(() => {});
        } else {
            navigator.clipboard.writeText(`${data.text} ${data.url}`);
            alert('Lenken er kopiert. Du kan nå lime den inn i sosiale medier.');
        }
    }

    return (
        <button className="share" onClick={del} title="Del kurs" aria-label="Del kurs">
            <span><i className="fa-solid fa-share-nodes"></i></span>
        </button>
    );
}

export default ShareButton;
