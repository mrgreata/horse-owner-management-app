import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseCreate, HorseSearch} from '../dto/horse';
import {formatIsoDate} from '../utils/date-helper';

const baseUri = environment.backendUrl + '/horses';

@Injectable({ providedIn: 'root' })
export class HorseService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<Horse[]> {
    return this.http.get<any[]>(baseUri).pipe(map(hs => hs.map(this.fixHorse)));
  }

  /** Suche mit kombinierbaren Parametern (für Autocomplete Mother/Father wichtig) */
  search(params: HorseSearch): Observable<Horse[]> {
    let httpParams = new HttpParams();
    if (params.name) httpParams = httpParams.set('name', params.name);
    if (params.description) httpParams = httpParams.set('description', params.description);
    if (params.sex) httpParams = httpParams.set('sex', params.sex);
    if (params.ownerName) httpParams = httpParams.set('ownerName', params.ownerName);
    if (params.limit != null) httpParams = httpParams.set('limit', String(params.limit));
    if (params.bornBefore) httpParams = httpParams.set('bornBefore', formatIsoDate(params.bornBefore));

    return this.http.get<any[]>(baseUri, { params: httpParams })
      .pipe(map(hs => hs.map(this.fixHorse)));
  }

  create(horse: HorseCreate): Observable<Horse> {
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    return this.http.post<any>(baseUri, horse).pipe(map(this.fixHorse));
  }

  getById(id: number): Observable<Horse> {
    return this.http.get<any>(`${baseUri}/${id}`).pipe(map(this.fixHorse));
  }

  update(id: number, horse: HorseCreate): Observable<Horse> {
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    return this.http.put<any>(`${baseUri}/${id}`, horse).pipe(map(this.fixHorse));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${baseUri}/${id}`);
  }

  /** Normalisiert Datum + Owner (first/last statt name) */
  private fixHorse = (raw: any): Horse => {
    const owner = raw.owner
      ? {
        id: raw.owner.id,
        firstName: raw.owner.firstName,
        lastName:  raw.owner.lastName,
        email:     raw.owner.email ?? null
      }
      : null;

    return {
      ...raw,
      owner,
      dateOfBirth: new Date(raw.dateOfBirth as string)
    } as Horse;
  };

}
