import { useState } from 'react';
import './Pamelding.css';

// Må være lik MEMBER_PRICE_FACTOR i EnrollmentHandler.java. Backend regner ut den ekte prisen.
const MEDLEMSRABATT = 0.2;

function kr(belop) {
  return belop === 0 ? 'Gratis' : `${belop.toLocaleString('nb-NO', { maximumFractionDigits: 2 })} kr`;
}

function Pamelding({ courses, valgtKursId, user, setCurrentPage }) {
  const [form, setForm] = useState({
    courseId: valgtKursId ?? courses[0]?.id ?? '',
    fornavn: user?.fornavn ?? '',
    etternavn: user?.etternavn ?? '',
    alder: '',
    email: user?.email ?? '',
    erMedlem: Boolean(user),
  });
  const [feil, setFeil] = useState('');
  const [bekreftelse, setBekreftelse] = useState(null);
  const [sender, setSender] = useState(false);

  const kurs = courses.find(k => k.id === Number(form.courseId));
  const ordinaerPris = kurs ? Number(kurs.pris) : 0;
  const medlemspris = Math.round(ordinaerPris * (1 - MEDLEMSRABATT) * 100) / 100;
  const dinPris = form.erMedlem ? medlemspris : ordinaerPris;

  function oppdater(e) {
    const { name, value, type, checked } = e.target;
    setForm(f => ({ ...f, [name]: type === 'checkbox' ? checked : value }));
  }

  async function meldPa(e) {
    e.preventDefault();
    setFeil('');
    setSender(true);

    try {
      const res = await fetch('/api/enrollments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...form,
          courseId: Number(form.courseId),
          alder: Number(form.alder),
        }),
      });
      const data = await res.json();

      if (data.success) {
        setBekreftelse({ kurs, pris: Number(data.pris), navn: form.fornavn, email: form.email });
      } else {
        setFeil(data.message);
      }
    } catch {
      setFeil('Fikk ikke kontakt med serveren. Prøv igjen senere.');
    } finally {
      setSender(false);
    }
  }

  if (bekreftelse) {
    return (
        <main className="pamelding">
          <div className="pamelding-boks">
            <h2>Takk, {bekreftelse.navn}!</h2>
            <p>Du er påmeldt <strong>{bekreftelse.kurs?.tittel}</strong>.</p>
            <p>Pris: <strong>{kr(bekreftelse.pris)}</strong></p>
            <p className="pamelding-hint">Vi bruker {bekreftelse.email} hvis vi må kontakte deg om kurset.</p>
            <button className="btn-secondary" onClick={() => setCurrentPage('home')}>Tilbake til kursene</button>
          </div>
        </main>
    );
  }

  return (
      <main className="pamelding">
        <form className="pamelding-boks" onSubmit={meldPa}>
          <h2>Påmelding</h2>

          <label>
            Kurs
            <select name="courseId" value={form.courseId} onChange={oppdater} required>
              {courses.map(k => (
                  <option key={k.id} value={k.id}>{k.tittel} ({k.dato})</option>
              ))}
            </select>
          </label>

          <div className="pamelding-rad">
            <label>
              Fornavn
              <input name="fornavn" value={form.fornavn} onChange={oppdater} maxLength={50} required />
            </label>
            <label>
              Etternavn
              <input name="etternavn" value={form.etternavn} onChange={oppdater} maxLength={50} required />
            </label>
          </div>

          <div className="pamelding-rad">
            <label>
              Alder
              <input name="alder" type="number" min={1} max={120} value={form.alder} onChange={oppdater} required />
            </label>
            <label>
              E-post
              <input name="email" type="email" value={form.email} onChange={oppdater} maxLength={100} required />
            </label>
          </div>

          <label className="pamelding-medlem">
            <input name="erMedlem" type="checkbox" checked={form.erMedlem} onChange={oppdater} />
            Jeg er medlem (20 % rabatt)
          </label>
          {form.erMedlem && (
              <p className="pamelding-hint">Bruk e-posten du registrerte deg med som medlem.</p>
          )}

          {kurs && (
              <div className="pamelding-pris">
                {ordinaerPris === 0 ? (
                    <span>Dette kurset er <strong>gratis</strong>.</span>
                ) : (
                    <>
                      <span>Ordinær pris: {kr(ordinaerPris)}</span>
                      <span>Medlemspris: {kr(medlemspris)}</span>
                      <span className="pamelding-total">Din pris: <strong>{kr(dinPris)}</strong></span>
                    </>
                )}
              </div>
          )}

          {feil && <p className="pamelding-feil">{feil}</p>}

          <div className="pamelding-knapper">
            <button type="button" className="pamelding-avbryt" onClick={() => setCurrentPage('home')}>Avbryt</button>
            <button type="submit" className="btn-secondary" disabled={sender}>
              {sender ? 'Melder på…' : 'Meld meg på'}
            </button>
          </div>
        </form>
      </main>
  );
}

export default Pamelding;
