using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers
{
    public class NotificacaoController : Controller
    {
        private readonly MiniCrmContext _context;

        public NotificacaoController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: Notificacao
        public async Task<IActionResult> Index()
        {
            var miniCrmContext = _context.Notificacao.Include(n => n.IdTarefaNavigation).Include(n => n.IdUsuarioNavigation);
            return View(await miniCrmContext.ToListAsync());
        }

        // GET: Notificacao/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var notificacao = await _context.Notificacao
                .Include(n => n.IdTarefaNavigation)
                .Include(n => n.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdNotificacao == id);
            if (notificacao == null)
            {
                return NotFound();
            }

            return View(notificacao);
        }

        // GET: Notificacao/Create
        public IActionResult Create()
        {
            ViewData["IdTarefa"] = new SelectList(_context.Tarefa, "IdTarefa", "Titulo");
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email");
            return View();
        }

        // POST: Notificacao/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("IdNotificacao,GeradaEm,Lida,Mensagem,Tipo,IdTarefa,IdUsuario")] Notificacao notificacao)
        {
            if (ModelState.IsValid)
            {
                _context.Add(notificacao);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdTarefa"] = new SelectList(_context.Tarefa, "IdTarefa", "Titulo", notificacao.IdTarefa);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", notificacao.IdUsuario);
            return View(notificacao);
        }

        // GET: Notificacao/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var notificacao = await _context.Notificacao.FindAsync(id);
            if (notificacao == null)
            {
                return NotFound();
            }
            ViewData["IdTarefa"] = new SelectList(_context.Tarefa, "IdTarefa", "Titulo", notificacao.IdTarefa);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", notificacao.IdUsuario);
            return View(notificacao);
        }

        // POST: Notificacao/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("IdNotificacao,GeradaEm,Lida,Mensagem,Tipo,IdTarefa,IdUsuario")] Notificacao notificacao)
        {
            if (id != notificacao.IdNotificacao)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(notificacao);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!NotificacaoExists(notificacao.IdNotificacao))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdTarefa"] = new SelectList(_context.Tarefa, "IdTarefa", "Titulo", notificacao.IdTarefa);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", notificacao.IdUsuario);
            return View(notificacao);
        }

        // GET: Notificacao/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var notificacao = await _context.Notificacao
                .Include(n => n.IdTarefaNavigation)
                .Include(n => n.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdNotificacao == id);
            if (notificacao == null)
            {
                return NotFound();
            }

            return View(notificacao);
        }

        // POST: Notificacao/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var notificacao = await _context.Notificacao.FindAsync(id);
            if (notificacao != null)
            {
                _context.Notificacao.Remove(notificacao);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool NotificacaoExists(int id)
        {
            return _context.Notificacao.Any(e => e.IdNotificacao == id);
        }
    }
}
