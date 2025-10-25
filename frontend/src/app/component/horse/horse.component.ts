import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AutocompleteComponent } from 'src/app/component/autocomplete/autocomplete.component';
import { HorseService } from 'src/app/service/horse.service';
import { Horse } from 'src/app/dto/horse';
import { Owner } from 'src/app/dto/owner';
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
import { Sex } from 'src/app/dto/sex';
import { DatePipe } from '@angular/common';
import {map} from "rxjs";

type MaybeDate = Date | null;

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  imports: [
    RouterLink,
    FormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent,
    DatePipe,
  ],
  standalone: true,
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {
  Sex = Sex;
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseForDeletion: Horse | undefined;


  // 🔎 Suchmodell (Template-Form)
  search = {
    name: '',
    description: '',
    bornBefore: null as MaybeDate,
    sex: null as Sex | null,   // Enum statt String
    ownerName: '',
    limit: null as number | null,
  };


  loading = false;

  constructor(
    private service: HorseService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.searchHorses();
  }

  /** true, wenn irgendein Filter gesetzt ist */
  private hasFilters(): boolean {
    const v = this.search;
    return !!(
      v.name ||
      v.description ||
      v.ownerName ||
      v.sex ||
      v.limit ||
      v.bornBefore
    );
  }

  /** Lädt je nach Filter entweder alle oder die gefilterte Liste */
  searchHorses(): void {
    this.loading = true;
    const src$ = this.hasFilters()
      ? this.service.search({
        name: this.search.name || undefined,
        description: this.search.description || undefined,
        ownerName: this.search.ownerName || undefined,
        sex: this.search.sex ?? undefined,
        limit: this.search.limit ?? undefined,
        bornBefore: this.search.bornBefore ?? undefined,
      })
      : this.service.getAll();

    src$.subscribe({
      next: data => {
        this.horses = data;
        this.bannerError = null;
        this.loading = false;
      },
      error: error => {
        console.error('Error fetching horses', error);
        this.loading = false;
        this.bannerError = 'Could not fetch horses: ' + (error?.message ?? 'Unknown error');
        const errorMessage = error.status === 0 ? 'Is the backend up?' : (error?.error?.message ?? error.message);
        this.notification.error(errorMessage, 'Could Not Fetch Horses');
      }
    });
  }

  /** Reset aller Filter */
  clearFilters(): void {
    this.search = {
      name: '',
      description: '',
      bornBefore: null,
      sex: null,              // 👈 zurück auf null
      ownerName: '',
      limit: null,
    };
    this.searchHorses();
  }

  reloadHorses(): void {
    this.searchHorses();
  }


  /** helper fürs Date-Input (yyyy-MM-dd -> Date) */
  onBornBeforeChange(value: string): void {
    this.search.bornBefore = value ? new Date(value) : null;
  }

  ownerName(owner: Owner | null | undefined): string {
    return owner?.name?.trim() || '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return horse.dateOfBirth.toLocaleDateString();
  }


  deleteHorse(horse: Horse) {
    if (!horse?.id) return;
    this.service.delete(horse.id).subscribe({
      next: () => {
        this.notification.success(`Horse ${horse.name} deleted.`);
        this.horseForDeletion = undefined;
        this.searchHorses(); // nach Löschen Liste neu laden (Filter bleiben erhalten)
      },
      error: err => {
        const msg = err?.error?.message ?? err?.message ?? 'Unknown error';
        this.notification.error(msg, 'Could Not Delete Horse');
      }
    });
  }

  // Vorschläge: einfache Stringliste mit Owner-Namen
  ownerSuggestions = (q: string) =>
    this.service.search({ ownerName: q, limit: 8 }).pipe(
      map(hs => {
        const names = hs
          .map(h => h.owner?.name ?? '')
          .filter(n => n);
        // eindeutige Namen
        return Array.from(new Set(names));
      })
    );

// Für String-Kandidaten trivial – könnte man auch weglassen
  ownerToText = (v: string | null) => v ?? '';



}
